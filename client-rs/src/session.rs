use std::collections::HashMap;
use std::net::SocketAddr;
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::time::Duration;

use anyhow::{anyhow, bail, Context, Result};
use bytes::{Bytes, BytesMut};
use futures_util::{SinkExt, StreamExt};
use prost::Message;
use tokio::io::{AsyncReadExt, AsyncWriteExt, ReadHalf, WriteHalf};
use tokio::net::{TcpStream, UdpSocket};
use tokio::sync::{mpsc, oneshot, Mutex, Notify};
use tokio::task::JoinHandle;
use tokio::time::{interval, sleep, Instant, MissedTickBehavior};
use tokio_util::codec::{FramedRead, FramedWrite};
use tokio_util::sync::CancellationToken;
use tracing::{debug, error, info, warn};
use uuid::Uuid;

use crate::config::AppConfig;
use crate::identity::AgentIdentity;
use crate::message::{
    build_auth_info, build_proxy_report, encode_message, make_status, status_code, status_message,
    status_pair, AuthResponse, BatchCreateProxiesResponse, CreateConnectionRequest,
    CreateConnectionResponse, Error as ProtoError, OpenStreamResponse, ProxySyncResponse,
    ProxySyncType,
};
use crate::protocol::{
    build_flags, decode_new_stream, is_datagram, is_encrypted, is_mux, opcode_name, Codec, Frame,
    TunnelTransport, MSG_AUTH, MSG_AUTH_RESP, MSG_CONFIG_SYNC, MSG_CONNECTION_CREATE,
    MSG_CONNECTION_CREATE_RESP, MSG_ERROR, MSG_GOAWAY, MSG_PING, MSG_PONG, MSG_PROXY_REPORT_REQ,
    MSG_PROXY_REPORT_RESP, MSG_STREAM_CLOSE, MSG_STREAM_DATA, MSG_STREAM_OPEN, MSG_STREAM_OPEN_RESP,
    MSG_STREAM_PAUSE, MSG_STREAM_RESET, MSG_STREAM_RESUME,
};
use crate::transport::{self, BoxedConn};

const HEARTBEAT_SECS: u64 = 30;
const IDLE_SECS: u64 = 90;
const MAX_MISSED_HEARTBEATS: u32 = 3;
const STABLE_SESSION_SECS: u64 = 3;

type FrameTx = mpsc::Sender<OutboundFrame>;
type ConnRead = ReadHalf<BoxedConn>;
type ConnWrite = WriteHalf<BoxedConn>;

struct OutboundFrame {
    frame: Frame,
    flushed: Option<oneshot::Sender<()>>,
}

impl OutboundFrame {
    fn new(frame: Frame) -> Self {
        Self {
            frame,
            flushed: None,
        }
    }

    fn with_flush_notify(frame: Frame) -> (Self, oneshot::Receiver<()>) {
        let (tx, rx) = oneshot::channel();
        (
            Self {
                frame,
                flushed: Some(tx),
            },
            rx,
        )
    }
}

pub async fn run_agent(config: AppConfig, mut shutdown: mpsc::Receiver<()>) -> Result<()> {
    let mut identity = AgentIdentity::load_default();
    let mut attempt: u32 = 0;

    loop {
        // 停止信号在 run_session / 退避等待中处理，避免与 select 双重借用 Receiver
        match run_session(&config, &mut identity, &mut shutdown).await {
            Ok(SessionEnd::Shutdown) => {
                info!("客户端已停止");
                return Ok(());
            }
            Ok(SessionEnd::ClearIdentityAndRetry) => {
                identity.clear();
                warn!("服务端返回 agent 不存在，已清除本地身份");
                attempt = 0;
            }
            Ok(SessionEnd::Reconnect { stable }) => {
                warn!("会话断开，准备重连");
                if stable {
                    attempt = 0;
                } else {
                    attempt += 1;
                }
                ensure_retries_left(&config, attempt)?;
            }
            Err(e) => {
                error!("会话异常: {e:#}");
                attempt += 1;
                if retries_exhausted(&config, attempt) {
                    return Err(e).context(format!(
                        "重连次数已达上限 ({})",
                        config.retry.max_retries
                    ));
                }
            }
        }

        let delay = backoff_delay(&config, attempt.max(1));
        info!("将在 {delay:?} 后进行第 {attempt} 次重连");
        tokio::select! {
            _ = shutdown.recv() => {
                info!("重连等待期间收到停止信号");
                return Ok(());
            }
            _ = sleep(delay) => {}
        }
    }
}

fn retries_exhausted(config: &AppConfig, attempt: u32) -> bool {
    config.retry.max_retries > 0 && attempt > config.retry.max_retries
}

fn ensure_retries_left(config: &AppConfig, attempt: u32) -> Result<()> {
    if retries_exhausted(config, attempt) {
        bail!("重连次数已达上限 ({})", config.retry.max_retries);
    }
    Ok(())
}

enum SessionEnd {
    Reconnect { stable: bool },
    ClearIdentityAndRetry,
    Shutdown,
}

fn backoff_delay(config: &AppConfig, attempt: u32) -> Duration {
    let base = config.retry.initial_delay_secs.max(1) as u64;
    let max = config.retry.max_delay_secs.max(1) as u64;
    let exp = attempt.saturating_sub(1).min(16);
    let delay = base.saturating_mul(1u64 << exp).min(max);
    let jitter = (delay as f64) * 0.3 * (rand_f64() - 0.5) * 2.0;
    Duration::from_secs_f64((delay as f64 + jitter).max(0.2))
}

fn rand_f64() -> f64 {
    use std::collections::hash_map::DefaultHasher;
    use std::hash::{Hash, Hasher};
    let mut h = DefaultHasher::new();
    Instant::now().hash(&mut h);
    std::process::id().hash(&mut h);
    (h.finish() as f64) / (u64::MAX as f64)
}

async fn dial_framed(
    config: &AppConfig,
    protocol: &str,
    stream_encrypt: bool,
) -> Result<(FramedRead<ConnRead, Codec>, FramedWrite<ConnWrite, Codec>)> {
    let port = transport::resolve_endpoint_port(
        protocol,
        config.server_port,
        &config.transport.quic,
        &config.transport.websocket,
    );
    let conn_encrypt = transport::resolve_effective_encrypt(
        protocol,
        config.transport.tls.enabled,
        Some(stream_encrypt),
    );
    let stream = transport::dial(
        protocol,
        &config.server_addr,
        port,
        conn_encrypt,
        &config.transport.tls,
        &config.transport.quic,
        &config.transport.websocket,
    )
    .await?;
    let (read_half, write_half) = tokio::io::split(stream);
    Ok((
        FramedRead::new(read_half, Codec::default()),
        FramedWrite::new(write_half, Codec::default()),
    ))
}

async fn run_session(
    config: &AppConfig,
    identity: &mut AgentIdentity,
    shutdown: &mut mpsc::Receiver<()>,
) -> Result<SessionEnd> {
    let started = Instant::now();
    let (mut framed_read, framed_write) = dial_framed(
        config,
        &config.transport.protocol,
        config.transport.tls.enabled,
    )
    .await?;

    let (frame_tx, frame_rx) = mpsc::channel::<OutboundFrame>(256);
    let writer_task = tokio::spawn(frame_writer(framed_write, frame_rx));

    let auth = build_auth_info(config, identity.current());
    frame_tx
        .send(OutboundFrame::new(Frame::new(
            0,
            MSG_AUTH,
            encode_message(&auth),
        )))
        .await
        .context("发送鉴权请求失败")?;
    debug!("已发送鉴权请求");

    let auth_frame = match recv_until(&mut framed_read, MSG_AUTH_RESP).await {
        Ok(f) => f,
        Err(RecvUntilError::RemoteStop) => {
            info!("收到服务端停止指令，准备停止客户端");
            stop_writer(writer_task, frame_tx).await;
            return Ok(SessionEnd::Shutdown);
        }
        Err(RecvUntilError::Other(e)) => return Err(e),
    };
    let auth_resp =
        AuthResponse::decode(auth_frame.payload).context("解析 AuthResponse 失败")?;
    let (status_code, status_msg) = status_pair(auth_resp.status);
    match status_code {
        0 => {
            if !auth_resp.agent_id.is_empty() {
                identity.update(&auth_resp.agent_id);
            }
            info!(
                "鉴权成功 agent_id={} connection_id={}",
                auth_resp.agent_id, auth_resp.connection_id
            );
        }
        100 => {
            stop_writer(writer_task, frame_tx).await;
            error!("鉴权失败：无效 token，停止客户端");
            return Ok(SessionEnd::Shutdown);
        }
        101 => {
            stop_writer(writer_task, frame_tx).await;
            return Ok(SessionEnd::ClearIdentityAndRetry);
        }
        other => {
            stop_writer(writer_task, frame_tx).await;
            error!("鉴权失败 code={other} message={status_msg}，停止客户端");
            return Ok(SessionEnd::Shutdown);
        }
    }
    let connection_id = auth_resp.connection_id;

    let report = build_proxy_report(config);
    frame_tx
        .send(OutboundFrame::new(Frame::new(
            0,
            MSG_PROXY_REPORT_REQ,
            encode_message(&report),
        )))
        .await
        .context("发送代理上报失败")?;
    info!("已上报 {} 个代理", report.proxies.len());

    let end = serve_loop(config, connection_id, framed_read, frame_tx.clone(), shutdown).await;
    let end = match end {
        Ok(SessionEnd::Reconnect { .. }) => Ok(SessionEnd::Reconnect {
            stable: started.elapsed() >= Duration::from_secs(STABLE_SESSION_SECS),
        }),
        other => other,
    };

    let _ = frame_tx
        .send(OutboundFrame::new(Frame::empty(0, MSG_GOAWAY)))
        .await;
    stop_writer(writer_task, frame_tx).await;
    end
}

async fn stop_writer(mut writer_task: JoinHandle<()>, frame_tx: FrameTx) {
    drop(frame_tx);
    tokio::select! {
        _ = &mut writer_task => {}
        _ = sleep(Duration::from_secs(1)) => {
            writer_task.abort();
            let _ = writer_task.await;
        }
    }
}

async fn frame_writer(
    mut writer: FramedWrite<ConnWrite, Codec>,
    mut rx: mpsc::Receiver<OutboundFrame>,
) {
    while let Some(out) = rx.recv().await {
        if out.frame.msg_type == MSG_STREAM_DATA {
            tracing::trace!(
                "发送帧 type=STREAM_DATA stream_id={} bytes={}",
                out.frame.stream_id,
                out.frame.payload.len()
            );
        } else {
            debug!(
                "发送帧 type={} stream_id={}",
                opcode_name(out.frame.msg_type),
                out.frame.stream_id
            );
        }
        let ok = match writer.send(out.frame).await {
            Ok(()) => writer.flush().await.is_ok(),
            Err(_) => false,
        };
        if let Some(notify) = out.flushed {
            let _ = notify.send(());
        }
        if !ok {
            break;
        }
    }
}

enum RecvUntilError {
    RemoteStop,
    Other(anyhow::Error),
}

async fn recv_until(
    reader: &mut FramedRead<ConnRead, Codec>,
    expect: u8,
) -> Result<Frame, RecvUntilError> {
    let deadline = sleep(Duration::from_secs(30));
    tokio::pin!(deadline);
    loop {
        tokio::select! {
            _ = &mut deadline => {
                return Err(RecvUntilError::Other(anyhow!(
                    "等待消息超时: {}",
                    opcode_name(expect)
                )));
            }
            item = reader.next() => {
                let frame = match item {
                    Some(Ok(f)) => f,
                    Some(Err(e)) => {
                        return Err(RecvUntilError::Other(e.into()));
                    }
                    None => {
                        return Err(RecvUntilError::Other(anyhow!("控制连接已关闭")));
                    }
                };
                match frame.msg_type {
                    x if x == expect => return Ok(frame),
                    MSG_PING | MSG_PONG => {}
                    MSG_GOAWAY => return Err(RecvUntilError::RemoteStop),
                    MSG_ERROR => {
                        let err = ProtoError::decode(frame.payload).unwrap_or_default();
                        let (code, msg) = status_pair(err.status);
                        return Err(RecvUntilError::Other(anyhow!(
                            "服务端错误 code={code} message={msg}"
                        )));
                    }
                    other => {
                        debug!("握手阶段忽略意外消息 type={}", opcode_name(other));
                    }
                }
            }
        }
    }
}

/// 控制通道异常结束时短时清空读缓冲，避免错过已到达的 GOAWAY
async fn conclude_control_disconnect(
    reader: &mut FramedRead<ConnRead, Codec>,
) -> SessionEnd {
    let deadline = Instant::now() + Duration::from_millis(200);
    loop {
        let left = deadline.saturating_duration_since(Instant::now());
        if left.is_zero() {
            break;
        }
        match tokio::time::timeout(left, reader.next()).await {
            Ok(Some(Ok(frame))) if frame.msg_type == MSG_GOAWAY => {
                info!("收到服务端停止指令，准备停止客户端");
                return SessionEnd::Shutdown;
            }
            Ok(Some(Ok(_))) => continue,
            Ok(Some(Err(_))) | Ok(None) | Err(_) => break,
        }
    }
    SessionEnd::Reconnect { stable: false }
}

struct StreamHandle {
    to_local: mpsc::Sender<Bytes>,
    paused: Arc<AtomicBool>,
    resume_notify: Arc<Notify>,
    /// 远端关流或会话拆毁时取消本地桥接
    cancel: CancellationToken,
    datagram: bool,
}

#[derive(Default)]
struct MuxSlots {
    map: HashMap<(String, bool), MuxTunnelState>,
}

impl MuxSlots {
    fn get(&self, protocol: &str, encrypt: bool) -> Option<&MuxTunnelState> {
        self.map
            .get(&(protocol.to_ascii_lowercase(), encrypt))
    }

    fn insert(&mut self, protocol: &str, state: MuxTunnelState) {
        let key = (protocol.to_ascii_lowercase(), state.encrypt);
        if let Some(old) = self.map.insert(key, state) {
            old.shutdown();
        }
    }

    fn clear(&mut self) {
        for (_, t) in self.map.drain() {
            t.shutdown();
        }
    }
}

struct MuxTunnelState {
    tunnel_id: String,
    encrypt: bool,
    frame_tx: FrameTx,
    writer: JoinHandle<()>,
    reader: JoinHandle<()>,
}

impl MuxTunnelState {
    fn shutdown(self) {
        drop(self.frame_tx);
        self.writer.abort();
        self.reader.abort();
    }
}

async fn serve_loop(
    config: &AppConfig,
    connection_id: u32,
    mut framed_read: FramedRead<ConnRead, Codec>,
    control_tx: FrameTx,
    shutdown: &mut mpsc::Receiver<()>,
) -> Result<SessionEnd> {
    let mut heartbeat = interval(Duration::from_secs(HEARTBEAT_SECS));
    heartbeat.set_missed_tick_behavior(MissedTickBehavior::Delay);
    let mut last_rx = Instant::now();
    let mut missed = 0u32;

    let streams: Arc<Mutex<HashMap<u32, StreamHandle>>> = Arc::new(Mutex::new(HashMap::new()));
    let mux: Arc<Mutex<MuxSlots>> = Arc::new(Mutex::new(MuxSlots::default()));
    // 串行化隧道创建，避免并发 STREAM_OPEN 打出多条同类型隧道
    let mux_create: Arc<Mutex<()>> = Arc::new(Mutex::new(()));
    let session_cancel = CancellationToken::new();

    let end = loop {
        tokio::select! {
            _ = shutdown.recv() => {
                info!("会话运行中收到停止信号");
                break Ok(SessionEnd::Shutdown);
            }
            _ = heartbeat.tick() => {
                let threshold =
                    Duration::from_secs(IDLE_SECS.saturating_mul((missed as u64).saturating_add(1)));
                if last_rx.elapsed() >= threshold {
                    missed += 1;
                    warn!("控制连接读空闲，连续未收到帧 {} 次", missed);
                    if missed >= MAX_MISSED_HEARTBEATS {
                        break Ok(SessionEnd::Reconnect { stable: false });
                    }
                }
                if control_tx
                    .send(OutboundFrame::new(Frame::empty(0, MSG_PING)))
                    .await
                    .is_err()
                {
                    // 写失败时优先排空读侧，避免错过强退/删除的 GOAWAY
                    break Ok(conclude_control_disconnect(&mut framed_read).await);
                }
            }
            item = framed_read.next() => {
                let Some(item) = item else {
                    break Ok(conclude_control_disconnect(&mut framed_read).await);
                };
                let frame = match item {
                    Ok(f) => f,
                    Err(e) => {
                        error!("控制帧解码失败: {e}");
                        break Ok(conclude_control_disconnect(&mut framed_read).await);
                    }
                };
                last_rx = Instant::now();
                missed = 0;

                match frame.msg_type {
                    MSG_GOAWAY => {
                        info!("收到服务端停止指令，准备停止客户端");
                        break Ok(SessionEnd::Shutdown);
                    }
                    MSG_ERROR => {
                        let err = ProtoError::decode(frame.payload).unwrap_or_default();
                        let (code, msg) = status_pair(err.status);
                        error!("收到服务端 ERROR code={code} message={msg}");
                        break Ok(SessionEnd::Reconnect { stable: false });
                    }
                    MSG_PROXY_REPORT_RESP => {
                        handle_proxy_report_resp(frame.payload);
                    }
                    MSG_CONFIG_SYNC => {
                        handle_config_sync(frame.payload);
                    }
                    MSG_STREAM_OPEN => {
                        if let Err(e) = handle_stream_open(
                            config,
                            connection_id,
                            frame,
                            control_tx.clone(),
                            streams.clone(),
                            mux.clone(),
                            mux_create.clone(),
                            session_cancel.clone(),
                        ).await {
                            debug!("处理 STREAM_OPEN 失败: {e:#}");
                        }
                    }
                    other => {
                        let stream_id = frame.stream_id;
                        if !handle_stream_side_frame(&streams, other, frame, Some(&control_tx))
                            .await
                        {
                            debug!(
                                "忽略控制消息 type={} stream_id={}",
                                opcode_name(other),
                                stream_id
                            );
                        }
                    }
                }
            }
        }
    };

    session_cancel.cancel();
    {
        let mut map = streams.lock().await;
        for (_, handle) in map.drain() {
            handle.cancel.cancel();
        }
    }
    mux.lock().await.clear();

    end
}

async fn handle_stream_side_frame(
    streams: &Arc<Mutex<HashMap<u32, StreamHandle>>>,
    msg_type: u8,
    frame: Frame,
    reply_tx: Option<&FrameTx>,
) -> bool {
    match msg_type {
        MSG_PONG => true,
        MSG_PING => {
            if let Some(tx) = reply_tx {
                let _ = tx
                    .send(OutboundFrame::new(Frame::empty(0, MSG_PONG)))
                    .await;
            }
            true
        }
        MSG_STREAM_CLOSE | MSG_STREAM_RESET => {
            close_stream(streams, frame.stream_id).await;
            true
        }
        MSG_STREAM_PAUSE => {
            if let Some(h) = streams.lock().await.get(&frame.stream_id) {
                if !h.datagram {
                    h.paused.store(true, Ordering::SeqCst);
                    debug!("流暂停 stream_id={}", frame.stream_id);
                }
            }
            true
        }
        MSG_STREAM_RESUME => {
            if let Some(h) = streams.lock().await.get(&frame.stream_id) {
                if !h.datagram {
                    h.paused.store(false, Ordering::SeqCst);
                    h.resume_notify.notify_waiters();
                    debug!("流恢复 stream_id={}", frame.stream_id);
                }
            }
            true
        }
        MSG_STREAM_DATA => {
            dispatch_to_local(streams, frame.stream_id, frame.payload).await;
            true
        }
        _ => false,
    }
}

fn handle_proxy_report_resp(payload: Bytes) {
    match BatchCreateProxiesResponse::decode(payload) {
        Ok(resp) => {
            let code = status_code(&resp.status);
            if code != 0 {
                let msg = status_message(resp.status);
                error!("代理上报失败 code={code} message={msg}");
                return;
            }
            info!("代理上报完成，本地代理 {} 条", resp.items.len());
            for item in &resp.items {
                debug!(
                    "代理就绪 name={} proxy_id={} remote={:?}",
                    item.name, item.proxy_id, item.remote_addr
                );
            }
        }
        Err(e) => error!("解析 PROXY_REPORT_RESP 失败: {e}"),
    }
}

fn handle_config_sync(payload: Bytes) {
    match ProxySyncResponse::decode(payload) {
        Ok(sync) => {
            let kind = ProxySyncType::try_from(sync.proxy_sync_type)
                .unwrap_or(ProxySyncType::Full);
            match kind {
                ProxySyncType::Delete => {
                    info!(
                        "收到服务端代理同步 DELETE，共 {} 条",
                        sync.proxy_ids.len()
                    );
                    debug!("DELETE proxy_ids={:?}", sync.proxy_ids);
                }
                other => {
                    info!(
                        "收到服务端代理同步 {:?}，共 {} 条",
                        other,
                        sync.items.len()
                    );
                    for item in &sync.items {
                        debug!(
                            "服务端代理 name={} proxy_id={} targets={} remote={:?}",
                            item.name,
                            item.proxy_id,
                            item.targets.len(),
                            item.remote_addr
                        );
                    }
                }
            }
        }
        Err(e) => error!("解析 CONFIG_SYNC 失败: {e}"),
    }
}

async fn close_stream(streams: &Arc<Mutex<HashMap<u32, StreamHandle>>>, stream_id: u32) {
    if let Some(handle) = streams.lock().await.remove(&stream_id) {
        handle.cancel.cancel();
        debug!("流已关闭 stream_id={stream_id}");
    }
}

async fn dispatch_to_local(
    streams: &Arc<Mutex<HashMap<u32, StreamHandle>>>,
    stream_id: u32,
    payload: Bytes,
) {
    if payload.is_empty() {
        return;
    }
    let (tx, datagram) = {
        let guard = streams.lock().await;
        match guard.get(&stream_id) {
            Some(h) => (h.to_local.clone(), h.datagram),
            None => {
                debug!(
                    "丢弃无归属流的数据 stream_id={} bytes={}",
                    stream_id,
                    payload.len()
                );
                return;
            }
        }
    };
    // 避免慢消费者头阻塞控制/隧道读循环
    match tx.try_send(payload) {
        Ok(()) => {}
        Err(mpsc::error::TrySendError::Full(_)) if datagram => {
            debug!("UDP 下行背压，丢弃 datagram stream_id={stream_id}");
        }
        Err(mpsc::error::TrySendError::Full(_)) => {
            debug!("流下行背压溢出，关闭流 stream_id={stream_id}");
            close_stream(streams, stream_id).await;
        }
        Err(mpsc::error::TrySendError::Closed(_)) => {
            debug!("流下行通道已关闭 stream_id={stream_id}");
        }
    }
}

#[allow(clippy::too_many_arguments)]
async fn handle_stream_open(
    config: &AppConfig,
    connection_id: u32,
    frame: Frame,
    control_tx: FrameTx,
    streams: Arc<Mutex<HashMap<u32, StreamHandle>>>,
    mux: Arc<Mutex<MuxSlots>>,
    mux_create: Arc<Mutex<()>>,
    session_cancel: CancellationToken,
) -> Result<()> {
    let stream_id = frame.stream_id;
    let visit = decode_new_stream(frame.payload).context("解析 STREAM_OPEN 载荷失败")?;

    let encrypt = is_encrypted(frame.flags);
    let frame_mux = is_mux(frame.flags);
    let datagram = is_datagram(frame.flags);

    let tunnel_transport = visit.transport;
    let protocol = tunnel_transport.as_str();
    if !tunnel_transport.is_supported() {
        warn!("流 {stream_id} 数据隧道协议 {protocol} 不受支持");
        send_open_resp(
            &control_tx,
            stream_id,
            connection_id,
            "",
            1,
            "unsupported transport",
            encrypt,
            false,
            datagram,
        )
        .await?;
        return Ok(());
    }

    let multiplex = if datagram {
        true
    } else {
        transport::normalize_multiplex(protocol, frame_mux)
    };
    if !multiplex {
        warn!("流 {stream_id} 要求独立隧道(multiplex=false)，当前 Rust 客户端未实现");
        send_open_resp(
            &control_tx,
            stream_id,
            connection_id,
            "",
            1,
            "direct tunnel not supported",
            encrypt,
            false,
            false,
        )
        .await?;
        return Ok(());
    }

    debug!(
        "打开流 stream_id={stream_id} target={}:{} protocol={protocol} encrypt={encrypt} multiplex={multiplex} datagram={datagram}",
        visit.host, visit.port
    );

    if datagram {
        return open_datagram_stream(
            config,
            connection_id,
            stream_id,
            visit.host,
            visit.port,
            tunnel_transport,
            encrypt,
            multiplex,
            control_tx,
            streams,
            mux,
            mux_create,
            session_cancel,
        )
        .await;
    }

    let local = match TcpStream::connect((visit.host.as_str(), visit.port)).await {
        Ok(s) => {
            let _ = s.set_nodelay(true);
            s
        }
        Err(e) => {
            debug!("连接本地服务失败 {}:{} — {e}", visit.host, visit.port);
            send_open_resp(
                &control_tx,
                stream_id,
                connection_id,
                "",
                1,
                &format!("local connect failed: {e}"),
                encrypt,
                multiplex,
                false,
            )
            .await?;
            return Ok(());
        }
    };

    let (tunnel_tx, tunnel_id) = ensure_mux_tunnel(
        config,
        connection_id,
        tunnel_transport,
        encrypt,
        multiplex,
        mux,
        streams.clone(),
        mux_create,
    )
    .await?;

    // 先注册下行通道，避免 OPEN 成功后的访问者数据丢失
    let (to_local_tx, to_local_rx) = mpsc::channel::<Bytes>(1024);
    let paused = Arc::new(AtomicBool::new(false));
    let resume_notify = Arc::new(Notify::new());
    let stream_cancel = session_cancel.child_token();
    streams.lock().await.insert(
        stream_id,
        StreamHandle {
            to_local: to_local_tx,
            paused: paused.clone(),
            resume_notify: resume_notify.clone(),
            cancel: stream_cancel.clone(),
            datagram: false,
        },
    );

    send_open_resp(
        &control_tx,
        stream_id,
        connection_id,
        &tunnel_id,
        0,
        "",
        encrypt,
        multiplex,
        false,
    )
    .await?;

    let streams_cleanup = streams.clone();
    tokio::spawn(async move {
        let _ = bridge_stream(
            stream_id,
            local,
            to_local_rx,
            tunnel_tx,
            encrypt,
            multiplex,
            paused,
            resume_notify,
            stream_cancel,
        )
        .await;
        streams_cleanup.lock().await.remove(&stream_id);
        debug!("流桥接已清理 stream_id={stream_id}");
    });

    Ok(())
}

#[allow(clippy::too_many_arguments)]
async fn open_datagram_stream(
    config: &AppConfig,
    connection_id: u32,
    stream_id: u32,
    host: String,
    port: u16,
    tunnel_transport: TunnelTransport,
    encrypt: bool,
    multiplex: bool,
    control_tx: FrameTx,
    streams: Arc<Mutex<HashMap<u32, StreamHandle>>>,
    mux: Arc<Mutex<MuxSlots>>,
    mux_create: Arc<Mutex<()>>,
    session_cancel: CancellationToken,
) -> Result<()> {
    let target = match resolve_udp_target(&host, port).await {
        Ok(addr) => addr,
        Err(e) => {
            debug!("解析 UDP 目标失败 {host}:{port} — {e:#}");
            send_open_resp(
                &control_tx,
                stream_id,
                connection_id,
                "",
                1,
                &format!("udp resolve failed: {e}"),
                encrypt,
                multiplex,
                true,
            )
            .await?;
            return Ok(());
        }
    };

    let socket = match bind_udp_ephemeral(target).await {
        Ok(s) => s,
        Err(e) => {
            debug!("绑定本地 UDP 失败 stream_id={stream_id}: {e}");
            send_open_resp(
                &control_tx,
                stream_id,
                connection_id,
                "",
                1,
                &format!("udp bind failed: {e}"),
                encrypt,
                multiplex,
                true,
            )
            .await?;
            return Ok(());
        }
    };

    let (tunnel_tx, tunnel_id) = match ensure_mux_tunnel(
        config,
        connection_id,
        tunnel_transport,
        encrypt,
        multiplex,
        mux,
        streams.clone(),
        mux_create,
    )
    .await
    {
        Ok(v) => v,
        Err(e) => {
            debug!("获取 UDP 数据隧道失败 stream_id={stream_id}: {e:#}");
            send_open_resp(
                &control_tx,
                stream_id,
                connection_id,
                "",
                1,
                &format!("tunnel unavailable: {e}"),
                encrypt,
                multiplex,
                true,
            )
            .await?;
            return Ok(());
        }
    };

    let (to_local_tx, to_local_rx) = mpsc::channel::<Bytes>(1024);
    let paused = Arc::new(AtomicBool::new(false));
    let resume_notify = Arc::new(Notify::new());
    let stream_cancel = session_cancel.child_token();
    streams.lock().await.insert(
        stream_id,
        StreamHandle {
            to_local: to_local_tx,
            paused,
            resume_notify,
            cancel: stream_cancel.clone(),
            datagram: true,
        },
    );

    send_open_resp(
        &control_tx,
        stream_id,
        connection_id,
        &tunnel_id,
        0,
        "",
        encrypt,
        multiplex,
        true,
    )
    .await?;

    let streams_cleanup = streams.clone();
    tokio::spawn(async move {
        let _ = bridge_datagram(
            stream_id,
            socket,
            target,
            to_local_rx,
            tunnel_tx,
            encrypt,
            multiplex,
            stream_cancel,
        )
        .await;
        streams_cleanup.lock().await.remove(&stream_id);
        debug!("UDP 流桥接已清理 stream_id={stream_id}");
    });

    Ok(())
}

async fn resolve_udp_target(host: &str, port: u16) -> Result<SocketAddr> {
    let mut addrs = tokio::net::lookup_host((host, port))
        .await
        .with_context(|| format!("lookup_host {host}:{port}"))?;
    addrs
        .next()
        .ok_or_else(|| anyhow!("无可用地址 {host}:{port}"))
}

async fn bind_udp_ephemeral(target: SocketAddr) -> Result<UdpSocket> {
    let bind_addr: SocketAddr = if target.is_ipv6() {
        "[::]:0".parse().unwrap()
    } else {
        "0.0.0.0:0".parse().unwrap()
    };
    UdpSocket::bind(bind_addr)
        .await
        .with_context(|| format!("bind UDP {bind_addr}"))
}

async fn send_open_resp(
    control_tx: &FrameTx,
    stream_id: u32,
    connection_id: u32,
    tunnel_id: &str,
    code: i32,
    message: &str,
    encrypt: bool,
    multiplex: bool,
    datagram: bool,
) -> Result<()> {
    let resp = OpenStreamResponse {
        connection_id,
        tunnel_id: tunnel_id.to_string(),
        status: make_status(code, message),
    };
    let frame = Frame::new(stream_id, MSG_STREAM_OPEN_RESP, encode_message(&resp))
        .with_flags(build_flags(multiplex, encrypt, datagram));
    // 等待帧 flush 到传输层后再读本地服务，避免握手数据早于流就绪
    let (out, flushed) = OutboundFrame::with_flush_notify(frame);
    control_tx
        .send(out)
        .await
        .context("发送 STREAM_OPEN_RESP 失败")?;
    match tokio::time::timeout(Duration::from_secs(5), flushed).await {
        Ok(Ok(())) => {}
        Ok(Err(_)) => debug!("STREAM_OPEN_RESP 写出通知通道已关闭 stream_id={stream_id}"),
        Err(_) => debug!("等待 STREAM_OPEN_RESP 写出超时 stream_id={stream_id}"),
    }
    Ok(())
}

#[allow(clippy::too_many_arguments)]
async fn ensure_mux_tunnel(
    config: &AppConfig,
    connection_id: u32,
    tunnel_transport: TunnelTransport,
    encrypt: bool,
    multiplex: bool,
    mux: Arc<Mutex<MuxSlots>>,
    streams: Arc<Mutex<HashMap<u32, StreamHandle>>>,
    mux_create: Arc<Mutex<()>>,
) -> Result<(FrameTx, String)> {
    let protocol = tunnel_transport.as_str();
    {
        let guard = mux.lock().await;
        if let Some(t) = guard.get(protocol, encrypt) {
            return Ok((t.frame_tx.clone(), t.tunnel_id.clone()));
        }
    }

    let _create_guard = mux_create.lock().await;
    {
        let guard = mux.lock().await;
        if let Some(t) = guard.get(protocol, encrypt) {
            return Ok((t.frame_tx.clone(), t.tunnel_id.clone()));
        }
    }

    let tunnel_id = Uuid::new_v4().to_string();
    debug!(
        "创建多路复用数据隧道 tunnel_id={tunnel_id} protocol={protocol} encrypt={encrypt} multiplex={multiplex}"
    );

    let (mut framed_read, mut framed_write) = dial_framed(config, protocol, encrypt).await?;

    let req = CreateConnectionRequest {
        tunnel_id: tunnel_id.clone(),
    };
    framed_write
        .send(
            Frame::new(
                connection_id,
                MSG_CONNECTION_CREATE,
                encode_message(&req),
            )
            .with_flags(build_flags(multiplex, encrypt, false)),
        )
        .await
        .context("发送 CONNECTION_CREATE 失败")?;
    framed_write
        .flush()
        .await
        .context("flush CONNECTION_CREATE 失败")?;

    let resp_frame = tokio::time::timeout(Duration::from_secs(15), async {
        loop {
            let frame = framed_read
                .next()
                .await
                .ok_or_else(|| anyhow!("数据隧道关闭"))?
                .context("解码数据隧道帧失败")?;
            if frame.msg_type == MSG_CONNECTION_CREATE_RESP {
                return Ok::<_, anyhow::Error>(frame);
            }
            debug!(
                "数据隧道握手阶段忽略 type={}",
                opcode_name(frame.msg_type)
            );
        }
    })
    .await
    .context("等待 CONNECTION_CREATE_RESP 超时")??;

    let resp = CreateConnectionResponse::decode(resp_frame.payload)
        .context("解析 CreateConnectionResponse 失败")?;
    let (code, msg) = status_pair(resp.status);
    if code != 0 {
        bail!("创建数据隧道失败 code={code} message={msg}");
    }
    let final_tunnel_id = if resp.tunnel_id.is_empty() {
        tunnel_id
    } else {
        resp.tunnel_id
    };

    let (tunnel_tx, tunnel_rx) = mpsc::channel::<OutboundFrame>(256);
    let writer = tokio::spawn(frame_writer(framed_write, tunnel_rx));
    let reader = tokio::spawn(tunnel_reader(framed_read, streams, tunnel_tx.clone()));

    let state = MuxTunnelState {
        tunnel_id: final_tunnel_id.clone(),
        encrypt,
        frame_tx: tunnel_tx.clone(),
        writer,
        reader,
    };
    mux.lock().await.insert(protocol, state);

    Ok((tunnel_tx, final_tunnel_id))
}

async fn tunnel_reader(
    mut framed_read: FramedRead<ConnRead, Codec>,
    streams: Arc<Mutex<HashMap<u32, StreamHandle>>>,
    tunnel_tx: FrameTx,
) {
    while let Some(item) = framed_read.next().await {
        let frame = match item {
            Ok(f) => f,
            Err(e) => {
                error!("数据隧道解码失败: {e}");
                break;
            }
        };
        let msg_type = frame.msg_type;
        let stream_id = frame.stream_id;
        if !handle_stream_side_frame(&streams, msg_type, frame, Some(&tunnel_tx)).await {
            debug!(
                "数据隧道忽略消息 type={} stream_id={}",
                opcode_name(msg_type),
                stream_id
            );
        }
    }
    warn!("数据隧道读循环结束");
}

#[allow(clippy::too_many_arguments)]
async fn bridge_stream(
    stream_id: u32,
    local: TcpStream,
    mut from_remote: mpsc::Receiver<Bytes>,
    tunnel_tx: FrameTx,
    encrypt: bool,
    multiplex: bool,
    paused: Arc<AtomicBool>,
    resume_notify: Arc<Notify>,
    cancel: CancellationToken,
) -> Result<()> {
    let flags = build_flags(multiplex, encrypt, false);
    let (mut local_read, mut local_write) = local.into_split();

    let upload_tx = tunnel_tx.clone();
    let paused_up = paused.clone();
    let resume_up = resume_notify.clone();
    let cancel_up = cancel.clone();
    let upload = async move {
        let mut buf = vec![0u8; 32 * 1024];
        loop {
            while paused_up.load(Ordering::SeqCst) {
                tokio::select! {
                    _ = cancel_up.cancelled() => return,
                    _ = resume_up.notified() => {}
                }
            }
            tokio::select! {
                _ = cancel_up.cancelled() => return,
                result = local_read.read(&mut buf) => {
                    match result {
                        Ok(0) => return,
                        Ok(n) => {
                            let frame = Frame::new(
                                stream_id,
                                MSG_STREAM_DATA,
                                Bytes::copy_from_slice(&buf[..n]),
                            )
                            .with_flags(flags);
                            if upload_tx
                                .send(OutboundFrame::new(frame))
                                .await
                                .is_err()
                            {
                                return;
                            }
                        }
                        Err(e) => {
                            debug!("本地读结束 stream_id={stream_id}: {e}");
                            return;
                        }
                    }
                }
            }
        }
    };

    let cancel_down = cancel.clone();
    let download = async move {
        loop {
            tokio::select! {
                _ = cancel_down.cancelled() => break,
                item = from_remote.recv() => {
                    match item {
                        Some(data) => {
                            if let Err(e) = local_write.write_all(&data).await {
                                debug!("本地写失败 stream_id={stream_id}: {e}");
                                break;
                            }
                        }
                        None => break,
                    }
                }
            }
        }
        let _ = local_write.shutdown().await;
    };

    tokio::select! {
        _ = cancel.cancelled() => {}
        _ = upload => {}
        _ = download => {}
    }

    let _ = tunnel_tx
        .send(OutboundFrame::new(
            Frame::empty(stream_id, MSG_STREAM_CLOSE).with_flags(flags),
        ))
        .await;
    Ok(())
}

async fn bridge_datagram(
    stream_id: u32,
    socket: UdpSocket,
    target: SocketAddr,
    mut from_remote: mpsc::Receiver<Bytes>,
    tunnel_tx: FrameTx,
    encrypt: bool,
    multiplex: bool,
    cancel: CancellationToken,
) -> Result<()> {
    let flags = build_flags(multiplex, encrypt, false);
    let socket = Arc::new(socket);

    let upload_tx = tunnel_tx.clone();
    let cancel_up = cancel.clone();
    let socket_up = socket.clone();
    let upload = async move {
        let mut buf = BytesMut::with_capacity(65536);
        loop {
            buf.clear();
            buf.reserve(65536);
            tokio::select! {
                _ = cancel_up.cancelled() => return,
                result = socket_up.recv_buf_from(&mut buf) => {
                    match result {
                        Ok((n, _)) => {
                            if n == 0 {
                                continue;
                            }
                            let payload = buf.split().freeze();
                            let frame = Frame::new(stream_id, MSG_STREAM_DATA, payload)
                                .with_flags(flags);
                            match upload_tx.try_send(OutboundFrame::new(frame)) {
                                Ok(()) => {}
                                Err(mpsc::error::TrySendError::Full(_)) => {
                                    debug!(
                                        "UDP 上行隧道背压，丢弃 datagram stream_id={stream_id}"
                                    );
                                }
                                Err(mpsc::error::TrySendError::Closed(_)) => return,
                            }
                        }
                        Err(e) => {
                            debug!("本地 UDP 读结束 stream_id={stream_id}: {e}");
                            return;
                        }
                    }
                }
            }
        }
    };

    let cancel_down = cancel.clone();
    let socket_down = socket;
    let download = async move {
        loop {
            tokio::select! {
                _ = cancel_down.cancelled() => break,
                item = from_remote.recv() => {
                    match item {
                        Some(data) => {
                            if let Err(e) = socket_down.send_to(&data, target).await {
                                debug!("本地 UDP 写失败 stream_id={stream_id}: {e}");
                                break;
                            }
                        }
                        None => break,
                    }
                }
            }
        }
    };

    tokio::select! {
        _ = cancel.cancelled() => {}
        _ = upload => {}
        _ = download => {}
    }

    let _ = tunnel_tx
        .send(OutboundFrame::new(
            Frame::empty(stream_id, MSG_STREAM_CLOSE).with_flags(flags),
        ))
        .await;
    Ok(())
}
