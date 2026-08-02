pub mod quic;
pub mod tcp;
pub mod tls;

use std::future::Future;
use std::pin::Pin;
use std::sync::Arc;

use anyhow::{bail, Result};
use tokio::io::{AsyncRead, AsyncWrite};

use crate::config::{QuicConfig, TlsConfig};

pub trait Conn: AsyncRead + AsyncWrite + Send + Unpin {}

impl<T> Conn for T where T: AsyncRead + AsyncWrite + Send + Unpin {}

pub type BoxedConn = Box<dyn Conn>;

pub(crate) type BoxFuture<'a, T> = Pin<Box<dyn Future<Output = T> + Send + 'a>>;

pub struct DialOptions<'a> {
    pub addr: &'a str,
    pub port: u16,
    pub encrypt: bool,
    pub tls: &'a TlsConfig,
    pub quic: &'a QuicConfig,
}

pub trait Transport: Send + Sync {
    fn name(&self) -> &'static str;
    fn dial<'a>(&'a self, opts: DialOptions<'a>) -> BoxFuture<'a, Result<BoxedConn>>;
}

pub fn supported_protocols() -> &'static [&'static str] {
    &["tcp", "quic"]
}

pub fn is_supported(protocol: &str) -> bool {
    supported_protocols()
        .iter()
        .any(|p| protocol.eq_ignore_ascii_case(p))
}

pub fn resolve(protocol: &str) -> Result<Arc<dyn Transport>> {
    match protocol.trim().to_ascii_lowercase().as_str() {
        "tcp" => Ok(Arc::new(tcp::TcpTransport)),
        "quic" => Ok(Arc::new(quic::QuicTransport)),
        other => bail!(
            "不支持的传输协议: \"{other}\"，当前支持: {}",
            supported_protocols().join(", ")
        ),
    }
}

pub fn resolve_endpoint_port(protocol: &str, server_port: u16, quic: &QuicConfig) -> u16 {
    match protocol.trim().to_ascii_lowercase().as_str() {
        "quic" => {
            if quic.port > 0 {
                quic.port
            } else {
                quic::DEFAULT_QUIC_PORT
            }
        }
        _ => server_port,
    }
}

pub async fn dial(
    protocol: &str,
    addr: &str,
    port: u16,
    encrypt: bool,
    tls: &TlsConfig,
    quic: &QuicConfig,
) -> Result<BoxedConn> {
    let transport = resolve(protocol)?;
    transport
        .dial(DialOptions {
            addr,
            port,
            encrypt,
            tls,
            quic,
        })
        .await
}

pub fn resolve_effective_encrypt(
    protocol: &str,
    global_tls_enabled: bool,
    proxy_encrypt: Option<bool>,
) -> bool {
    match protocol.trim().to_ascii_lowercase().as_str() {
        "quic" | "websocket" => true,
        _ => global_tls_enabled && proxy_encrypt.unwrap_or(true),
    }
}

pub fn normalize_multiplex(protocol: &str, multiplex: bool) -> bool {
    match protocol.trim().to_ascii_lowercase().as_str() {
        "quic" => true,
        _ => multiplex,
    }
}
