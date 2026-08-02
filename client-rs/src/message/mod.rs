#![allow(clippy::all)]
#![allow(dead_code)]

mod generated;
pub use generated::*;

use prost::Message;

use crate::config::{
    AppConfig, ProxyConfig, ProxyProtocol, ProxyTlsCertConfig, Socks5AuthConfig,
};
use crate::AGENT_VERSION;

pub fn build_auth_info(config: &AppConfig, agent_id: Option<&str>) -> AuthInfo {
    AuthInfo {
        token: config.auth.token.clone(),
        name: Some(config.auth.name.clone()),
        agent_type: AgentType::Binary as i32,
        version: AGENT_VERSION.to_string(),
        os: Some(std::env::consts::OS.to_string()),
        arch: Some(std::env::consts::ARCH.to_string()),
        agent_id: agent_id
            .filter(|s| !s.is_empty())
            .map(|s| s.to_string()),
    }
}

pub fn build_proxy_report(config: &AppConfig) -> BatchCreateProxiesRequest {
    let proxies = config.proxies.iter().map(proxy_to_proto).collect();
    BatchCreateProxiesRequest { proxies }
}

fn proxy_to_proto(p: &ProxyConfig) -> Proxy {
    let protocol = match p.protocol {
        ProxyProtocol::Tcp => ProtocolType::Tcp,
        ProxyProtocol::Http => ProtocolType::Http,
        ProxyProtocol::Https => ProtocolType::Https,
        ProxyProtocol::Udp => ProtocolType::Udp,
        ProxyProtocol::Socks5 => ProtocolType::Socks5,
    } as i32;

    let targets = p
        .targets
        .iter()
        .map(|t| Target {
            host: t.host.clone(),
            port: t.port,
            name: t.name.clone(),
            weight: Some(t.weight),
        })
        .collect();

    let domain = p.domain.as_ref().map(|d| Domain {
        auto_domain: Some(d.auto_domain),
        custom_domains: d.custom_domains.clone(),
        sub_domains: d.sub_domains.clone(),
    });

    let transport = transport_to_proto(&p.transport);

    let tls_cert = match (&p.protocol, &p.tls_cert) {
        (ProxyProtocol::Https, Some(cert)) => load_tls_cert(cert),
        _ => None,
    };

    let remote_port = match (p.protocol.reports_remote_port(), p.remote_port) {
        (true, Some(port)) if port > 0 => Some(port),
        _ => None,
    };

    let socks5_auth = match p.protocol {
        ProxyProtocol::Socks5 => p.socks5_auth.as_ref().map(socks5_auth_to_proto),
        _ => None,
    };

    Proxy {
        proxy_id: String::new(),
        name: p.name.clone(),
        protocol,
        enabled: p.enabled,
        targets,
        force_https: p.force_https,
        remote_port,
        domain,
        access_control: None,
        basic_auth: None,
        bandwidth: None,
        load_balance_strategy: None,
        transport,
        tls_cert,
        health_check: None,
        socks5_auth,
        file_auth: None,
        file_limits: None,
        header_rewrite: None,
        time_access: None,
    }
}

fn transport_to_proto(t: &crate::config::ProxyTransportConfig) -> Option<Transport> {
    let has_any = t.multiplex.is_some()
        || t.encrypt.is_some()
        || t.compress.is_some()
        || t.protocol.is_some();
    if !has_any {
        return None;
    }
    Some(Transport {
        multiplex: t.multiplex,
        encrypt: t.encrypt,
        compress: t.compress,
        protocol: t.protocol.clone(),
        compress_algorithm: None,
    })
}

fn socks5_auth_to_proto(auth: &Socks5AuthConfig) -> Socks5Auth {
    Socks5Auth {
        enabled: auth.enabled,
        users: auth
            .users
            .iter()
            .map(|u| Socks5User {
                username: u.username.clone(),
                password: u.password.clone(),
            })
            .collect(),
    }
}

fn load_tls_cert(cert: &ProxyTlsCertConfig) -> Option<TlsCert> {
    match (
        std::fs::read_to_string(&cert.key_file),
        std::fs::read_to_string(&cert.cert_file),
    ) {
        (Ok(private_key_pem), Ok(cert_chain_pem)) => Some(TlsCert {
            private_key_pem,
            cert_chain_pem,
        }),
        (Err(e), _) => {
            tracing::error!(
                "读取 TLS 私钥失败: {}: {e}",
                cert.key_file.display()
            );
            None
        }
        (_, Err(e)) => {
            tracing::error!(
                "读取 TLS 证书失败: {}: {e}",
                cert.cert_file.display()
            );
            None
        }
    }
}

pub fn encode_message<M: Message>(msg: &M) -> bytes::Bytes {
    let mut buf = Vec::with_capacity(msg.encoded_len());
    msg.encode(&mut buf).expect("protobuf 编码失败");
    bytes::Bytes::from(buf)
}

pub fn status_code(status: &Option<Status>) -> i32 {
    status.as_ref().map(|s| s.code).unwrap_or(-1)
}

pub fn status_message(status: Option<Status>) -> String {
    status.and_then(|s| s.message).unwrap_or_default()
}

pub fn status_pair(status: Option<Status>) -> (i32, String) {
    match status {
        Some(s) => (s.code, s.message.unwrap_or_default()),
        None => (-1, String::new()),
    }
}

pub fn make_status(code: i32, message: &str) -> Option<Status> {
    Some(Status {
        code,
        message: if message.is_empty() {
            None
        } else {
            Some(message.to_string())
        },
    })
}
