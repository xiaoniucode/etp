use std::pin::Pin;
use std::task::{Context, Poll};

use anyhow::{anyhow, bail, Context as _, Result};
use bytes::BytesMut;
use futures_util::{Sink, Stream};
use rustls::pki_types::ServerName;
use tokio::io::{AsyncRead, AsyncWrite, ReadBuf};
use tokio::net::TcpStream;
use tokio_rustls::client::TlsStream;
use tokio_rustls::TlsConnector;
use tokio_tungstenite::tungstenite::client::IntoClientRequest;
use tokio_tungstenite::tungstenite::protocol::WebSocketConfig as TungsteniteConfig;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::{client_async_with_config, WebSocketStream};
use tracing::debug;

use crate::config::{TlsConfig, WebSocketConfig};
use crate::transport::tls;
use crate::transport::{BoxFuture, BoxedConn, DialOptions, Transport};

pub const DEFAULT_WEBSOCKET_PORT: u16 = 9528;

pub struct WebSocketTransport;

impl Transport for WebSocketTransport {
    fn name(&self) -> &'static str {
        "websocket"
    }

    fn dial<'a>(&'a self, opts: DialOptions<'a>) -> BoxFuture<'a, Result<BoxedConn>> {
        Box::pin(async move {
            connect_wss(opts.addr, opts.port, opts.tls, opts.websocket).await
        })
    }
}

async fn connect_wss(
    addr: &str,
    port: u16,
    tls_cfg: &TlsConfig,
    ws_cfg: &WebSocketConfig,
) -> Result<BoxedConn> {
    if !tls_cfg.enabled {
        bail!("WebSocket 传输必须启用 TLS（对齐 Java WebSocketTransportConnector）");
    }

    let path = normalize_path(&ws_cfg.path);
    let target = format!("{addr}:{port}");
    let url = format!("wss://{addr}:{port}{path}");
    debug!(
        "正在连接 WebSocket {url}（max_frame_size={}）",
        ws_cfg.max_frame_size
    );

    let tcp = TcpStream::connect(&target)
        .await
        .with_context(|| format!("WebSocket TCP 连接失败: {target}"))?;
    tcp.set_nodelay(true).ok();

    let rustls_cfg = tls::build_client_config(tls_cfg)?;
    let connector = TlsConnector::from(rustls_cfg);
    let server_name = ServerName::try_from(addr.to_string())
        .map_err(|_| anyhow!("非法 TLS 服务器名: {addr}"))?;
    let tls = connector
        .connect(server_name, tcp)
        .await
        .with_context(|| format!("WebSocket TLS 握手失败: {target}"))?;

    let mut request = url
        .as_str()
        .into_client_request()
        .with_context(|| format!("构造 WebSocket 请求失败: {url}"))?;
    request.headers_mut().remove("Sec-WebSocket-Protocol");
    request.headers_mut().remove("Sec-WebSocket-Extensions");

    let mut ws_config = TungsteniteConfig::default();
    ws_config.max_message_size = Some(ws_cfg.max_frame_size);
    ws_config.max_frame_size = Some(ws_cfg.max_frame_size);

    let (ws, _response) = client_async_with_config(request, tls, Some(ws_config))
        .await
        .with_context(|| format!("WebSocket 握手失败: {url}"))?;
    debug!("WebSocket 握手完成 path={path}");

    Ok(Box::new(WsByteStream {
        ws,
        read_buf: BytesMut::new(),
    }))
}

fn normalize_path(path: &str) -> String {
    let p = path.trim();
    if p.is_empty() {
        return "/tunnel".to_string();
    }
    if p.starts_with('/') {
        p.to_string()
    } else {
        format!("/{p}")
    }
}

struct WsByteStream {
    ws: WebSocketStream<TlsStream<TcpStream>>,
    read_buf: BytesMut,
}

impl AsyncRead for WsByteStream {
    fn poll_read(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
        buf: &mut ReadBuf<'_>,
    ) -> Poll<std::io::Result<()>> {
        loop {
            if !self.read_buf.is_empty() {
                let n = buf.remaining().min(self.read_buf.len());
                buf.put_slice(&self.read_buf.split_to(n));
                return Poll::Ready(Ok(()));
            }

            match Pin::new(&mut self.ws).poll_next(cx) {
                Poll::Ready(Some(Ok(Message::Binary(data)))) => {
                    if data.is_empty() {
                        continue;
                    }
                    self.read_buf.extend_from_slice(&data);
                }
                Poll::Ready(Some(Ok(Message::Ping(_) | Message::Pong(_)))) => continue,
                Poll::Ready(Some(Ok(Message::Close(_)))) | Poll::Ready(None) => {
                    return Poll::Ready(Ok(()));
                }
                Poll::Ready(Some(Ok(Message::Text(_)))) => {
                    return Poll::Ready(Err(std::io::Error::new(
                        std::io::ErrorKind::InvalidData,
                        "WebSocket 收到文本帧，仅支持二进制 TMSP",
                    )));
                }
                Poll::Ready(Some(Ok(Message::Frame(_)))) => continue,
                Poll::Ready(Some(Err(e))) => {
                    return Poll::Ready(Err(std::io::Error::other(e)));
                }
                Poll::Pending => return Poll::Pending,
            }
        }
    }
}

impl AsyncWrite for WsByteStream {
    fn poll_write(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
        buf: &[u8],
    ) -> Poll<Result<usize, std::io::Error>> {
        if buf.is_empty() {
            return Poll::Ready(Ok(0));
        }
        match Pin::new(&mut self.ws).poll_ready(cx) {
            Poll::Ready(Ok(())) => {
                if let Err(e) = Pin::new(&mut self.ws)
                    .start_send(Message::Binary(bytes::Bytes::copy_from_slice(buf)))
                {
                    return Poll::Ready(Err(std::io::Error::other(e)));
                }
                Poll::Ready(Ok(buf.len()))
            }
            Poll::Ready(Err(e)) => Poll::Ready(Err(std::io::Error::other(e))),
            Poll::Pending => Poll::Pending,
        }
    }

    fn poll_flush(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
    ) -> Poll<Result<(), std::io::Error>> {
        match Pin::new(&mut self.ws).poll_flush(cx) {
            Poll::Ready(Ok(())) => Poll::Ready(Ok(())),
            Poll::Ready(Err(e)) => Poll::Ready(Err(std::io::Error::other(e))),
            Poll::Pending => Poll::Pending,
        }
    }

    fn poll_shutdown(
        mut self: Pin<&mut Self>,
        cx: &mut Context<'_>,
    ) -> Poll<Result<(), std::io::Error>> {
        match Pin::new(&mut self.ws).poll_close(cx) {
            Poll::Ready(Ok(())) => Poll::Ready(Ok(())),
            Poll::Ready(Err(e)) => Poll::Ready(Err(std::io::Error::other(e))),
            Poll::Pending => Poll::Pending,
        }
    }
}