use anyhow::{Context, Result};
use rustls::pki_types::ServerName;
use tokio::net::TcpStream;
use tokio_rustls::TlsConnector;
use tracing::debug;

use crate::config::TlsConfig;
use crate::transport::tls;
use crate::transport::{BoxFuture, BoxedConn, DialOptions, Transport};

pub struct TcpTransport;

impl Transport for TcpTransport {
    fn name(&self) -> &'static str {
        "tcp"
    }

    fn dial<'a>(&'a self, opts: DialOptions<'a>) -> BoxFuture<'a, Result<BoxedConn>> {
        Box::pin(async move {
            connect_stream(opts.addr, opts.port, opts.encrypt, opts.tls).await
        })
    }
}

async fn connect_stream(
    addr: &str,
    port: u16,
    use_tls: bool,
    tls_cfg: &TlsConfig,
) -> Result<BoxedConn> {
    let target = format!("{addr}:{port}");
    debug!("正在连接 {target}（tls={use_tls}）");
    let tcp = TcpStream::connect(&target)
        .await
        .with_context(|| format!("连接失败: {target}"))?;
    tcp.set_nodelay(true).ok();

    if !use_tls {
        return Ok(Box::new(tcp));
    }

    let config = tls::build_client_config(tls_cfg)?;
    let connector = TlsConnector::from(config);
    let server_name = ServerName::try_from(addr.to_string())
        .map_err(|_| anyhow::anyhow!("非法 TLS 服务器名: {addr}"))?;
    let tls_stream = connector
        .connect(server_name, tcp)
        .await
        .with_context(|| format!("TLS 握手失败: {target}"))?;
    Ok(Box::new(tls_stream))
}
