pub mod tcp;
pub mod tls;

use std::future::Future;
use std::pin::Pin;
use std::sync::Arc;

use anyhow::{bail, Result};
use tokio::io::{AsyncRead, AsyncWrite};

use crate::config::TlsConfig;

pub trait Conn: AsyncRead + AsyncWrite + Send + Unpin {}

impl<T> Conn for T where T: AsyncRead + AsyncWrite + Send + Unpin {}

pub type BoxedConn = Box<dyn Conn>;

pub(crate) type BoxFuture<'a, T> = Pin<Box<dyn Future<Output = T> + Send + 'a>>;

pub struct DialOptions<'a> {
    pub addr: &'a str,
    pub port: u16,
    pub encrypt: bool,
    pub tls: &'a TlsConfig,
}

pub trait Transport: Send + Sync {
    fn name(&self) -> &'static str;
    fn dial<'a>(&'a self, opts: DialOptions<'a>) -> BoxFuture<'a, Result<BoxedConn>>;
}

pub fn supported_protocols() -> &'static [&'static str] {
    &["tcp"]
}

pub fn is_supported(protocol: &str) -> bool {
    supported_protocols()
        .iter()
        .any(|p| protocol.eq_ignore_ascii_case(p))
}

pub fn resolve(protocol: &str) -> Result<Arc<dyn Transport>> {
    match protocol.trim().to_ascii_lowercase().as_str() {
        "tcp" => Ok(Arc::new(tcp::TcpTransport)),
        other => bail!(
            "不支持的传输协议: \"{other}\"，当前支持: {}",
            supported_protocols().join(", ")
        ),
    }
}

pub async fn dial(
    protocol: &str,
    addr: &str,
    port: u16,
    encrypt: bool,
    tls: &TlsConfig,
) -> Result<BoxedConn> {
    let transport = resolve(protocol)?;
    transport
        .dial(DialOptions {
            addr,
            port,
            encrypt,
            tls,
        })
        .await
}
