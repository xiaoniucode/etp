use std::net::{SocketAddr, ToSocketAddrs};
use std::pin::Pin;
use std::sync::Arc;
use std::task::{Context, Poll};
use std::time::Duration;

use anyhow::{anyhow, bail, Context as _, Result};
use quinn::crypto::rustls::QuicClientConfig;
use quinn::{ClientConfig, Connection, Endpoint, IdleTimeout, RecvStream, SendStream, VarInt};
use tokio::io::{AsyncRead, AsyncWrite, ReadBuf};
use tracing::debug;

use crate::config::QuicConfig;
use crate::transport::tls;
use crate::transport::{BoxFuture, BoxedConn, DialOptions, Transport};

pub use crate::config::DEFAULT_QUIC_PORT;

pub struct QuicTransport;

impl Transport for QuicTransport {
    fn name(&self) -> &'static str {
        "quic"
    }

    fn dial<'a>(&'a self, opts: DialOptions<'a>) -> BoxFuture<'a, Result<BoxedConn>> {
        Box::pin(async move { connect_stream(opts.addr, opts.port, opts.tls, opts.quic).await })
    }
}

async fn connect_stream(
    addr: &str,
    port: u16,
    tls_cfg: &crate::config::TlsConfig,
    quic_cfg: &QuicConfig,
) -> Result<BoxedConn> {
    let target = format!("{addr}:{port}");
    debug!(
        "正在连接 QUIC {target}（ALPN=orbien, idle={}ms）",
        quic_cfg.max_idle_timeout_ms
    );

    let remote = resolve_addr(addr, port)?;
    let mut endpoint = Endpoint::client(SocketAddr::from(([0, 0, 0, 0], 0)))
        .context("绑定 QUIC UDP 本地端口失败")?;
    endpoint.set_default_client_config(build_quinn_client_config(tls_cfg, quic_cfg)?);

    let connecting = endpoint
        .connect(remote, addr)
        .with_context(|| format!("发起 QUIC 连接失败: {target}"))?;
    let conn = connecting
        .await
        .with_context(|| format!("QUIC 握手失败: {target}"))?;
    debug!(
        "QUIC 连接已建立 remote={} local={:?}",
        conn.remote_address(),
        endpoint.local_addr().ok()
    );

    let (send, recv) = conn
        .open_bi()
        .await
        .context("创建 QUIC 双向 stream 失败")?;
    debug!("QUIC 数据 stream 已创建 stream_id={}", send.id().index());

    Ok(Box::new(QuicBiStream {
        send,
        recv,
        conn,
        endpoint,
    }))
}

fn resolve_addr(host: &str, port: u16) -> Result<SocketAddr> {
    (host, port)
        .to_socket_addrs()
        .with_context(|| format!("解析地址失败: {host}:{port}"))?
        .next()
        .ok_or_else(|| anyhow!("无法解析地址: {host}:{port}"))
}

fn build_quinn_client_config(
    tls_cfg: &crate::config::TlsConfig,
    quic_cfg: &QuicConfig,
) -> Result<ClientConfig> {
    let rustls_cfg = tls::build_quic_client_config(tls_cfg)?;
    let quic_crypto = QuicClientConfig::try_from(rustls_cfg)
        .map_err(|e| anyhow!("构建 QUIC TLS 配置失败: {e}"))?;

    let mut transport = quinn::TransportConfig::default();
    let idle = IdleTimeout::try_from(Duration::from_millis(quic_cfg.max_idle_timeout_ms))
        .map_err(|_| anyhow!("非法 max_idle_timeout_ms: {}", quic_cfg.max_idle_timeout_ms))?;
    transport.max_idle_timeout(Some(idle));
    transport.receive_window(to_varint(quic_cfg.initial_max_data, "initial_max_data")?);
    transport.stream_receive_window(to_varint(
        quic_cfg.initial_max_stream_data,
        "initial_max_stream_data",
    )?);
    transport.max_concurrent_bidi_streams(VarInt::from_u32(quic_cfg.initial_max_streams_bidi));

    let mut client = ClientConfig::new(Arc::new(quic_crypto));
    client.transport_config(Arc::new(transport));
    Ok(client)
}

fn to_varint(value: u64, field: &str) -> Result<VarInt> {
    VarInt::from_u64(value).map_err(|_| anyhow!("非法 {field}: {value}"))
}

struct QuicBiStream {
    send: SendStream,
    recv: RecvStream,
    conn: Connection,
    endpoint: Endpoint,
}

impl Drop for QuicBiStream {
    fn drop(&mut self) {
        self.conn.close(0u32.into(), b"");
        self.endpoint.close(0u32.into(), b"");
    }
}

impl AsyncRead for QuicBiStream {
    fn poll_read(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
        buf: &mut ReadBuf<'_>,
    ) -> Poll<std::io::Result<()>> {
        AsyncRead::poll_read(Pin::new(&mut self.recv), cx, buf)
    }
}

impl AsyncWrite for QuicBiStream {
    fn poll_write(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
        buf: &[u8],
    ) -> Poll<Result<usize, std::io::Error>> {
        AsyncWrite::poll_write(Pin::new(&mut self.send), cx, buf)
    }

    fn poll_flush(mut self: Pin<&mut Self>, cx: &mut Context<'_>) -> Poll<Result<(), std::io::Error>> {
        AsyncWrite::poll_flush(Pin::new(&mut self.send), cx)
    }

    fn poll_shutdown(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
    ) -> Poll<Result<(), std::io::Error>> {
        AsyncWrite::poll_shutdown(Pin::new(&mut self.send), cx)
    }
}

pub fn validate_quic_config(cfg: &QuicConfig) -> Result<()> {
    if cfg.port == 0 {
        bail!("配置错误: [transport.quic] server_port/port 无效");
    }
    if cfg.max_idle_timeout_ms == 0 {
        bail!("配置错误: [transport.quic] max_idle_timeout_ms 必须 > 0");
    }
    if cfg.initial_max_data == 0 || cfg.initial_max_stream_data == 0 {
        bail!("配置错误: [transport.quic] initial_max_data / initial_max_stream_data 必须 > 0");
    }
    if cfg.initial_max_streams_bidi == 0 {
        bail!("配置错误: [transport.quic] initial_max_streams_bidi 必须 > 0");
    }
    Ok(())
}
