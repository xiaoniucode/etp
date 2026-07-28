use std::collections::HashSet;
use std::fs;
use std::path::{Path, PathBuf};
use serde::Deserialize;
use thiserror::Error;
use tracing::warn;

#[derive(Debug, Clone)]
pub struct AppConfig {
    pub server_addr: String,
    pub server_port: u16,
    pub auth: AuthConfig,
    pub transport: TransportConfig,
    pub retry: RetryConfig,
    pub proxies: Vec<ProxyConfig>,
}

#[derive(Debug, Clone)]
pub struct AuthConfig {
    pub token: String,
    pub name: String,
}

#[derive(Debug, Clone)]
pub struct TransportConfig {
    pub protocol: String,
    pub multiplex: bool,
    pub tls: TlsConfig,
}

#[derive(Debug, Clone)]
pub struct TlsConfig {
    pub enabled: bool,
    pub ca_file: Option<PathBuf>,
    pub cert_file: Option<PathBuf>,
    pub key_file: Option<PathBuf>,
}

#[derive(Debug, Clone)]
pub struct RetryConfig {
    pub initial_delay_secs: u32,
    pub max_delay_secs: u32,
    pub max_retries: u32,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ProxyProtocol {
    Tcp,
    Http,
}

impl ProxyProtocol {
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Tcp => "tcp",
            Self::Http => "http",
        }
    }
}

#[derive(Debug, Clone)]
pub struct ProxyConfig {
    pub name: String,
    pub protocol: ProxyProtocol,
    pub enabled: bool,
    pub targets: Vec<TargetConfig>,
    pub remote_port: Option<u32>,
    pub domain: Option<DomainConfig>,
    pub transport: ProxyTransportConfig,
}

#[derive(Debug, Clone)]
pub struct TargetConfig {
    pub host: String,
    pub port: u32,
    pub name: Option<String>,
    pub weight: Option<i32>,
}

#[derive(Debug, Clone)]
pub struct DomainConfig {
    pub auto_domain: bool,
    pub custom_domains: Vec<String>,
    pub sub_domains: Vec<String>,
}

#[derive(Debug, Clone, Default)]
pub struct ProxyTransportConfig {
    pub multiplex: Option<bool>,
    pub encrypt: Option<bool>,
    pub compress: Option<bool>,
    pub protocol: Option<String>,
}

impl AppConfig {
    pub fn resolve_tunnel_encrypt(&self, proxy_encrypt: Option<bool>) -> bool {
        self.transport.tls.enabled && proxy_encrypt.unwrap_or(true)
    }
}

#[derive(Debug, Error)]
pub enum ConfigError {
    #[error("读取配置文件失败: {0}")]
    Io(#[from] std::io::Error),
    #[error("解析 TOML 失败: {0}")]
    Parse(#[from] toml::de::Error),
    #[error("{0}")]
    Invalid(String),
}

pub fn load_from_path(path: impl AsRef<Path>) -> Result<AppConfig, ConfigError> {
    let text = fs::read_to_string(path.as_ref())?;
    load_from_str(&text)
}

pub fn load_from_str(text: &str) -> Result<AppConfig, ConfigError> {
    let raw: RawRoot = toml::from_str(text)?;
    build_config(raw)
}

fn default_server_addr() -> String {
    "127.0.0.1".into()
}

fn default_server_port() -> u16 {
    9527
}

fn default_true() -> bool {
    true
}

fn default_retry_initial() -> u32 {
    1
}

fn default_retry_max_delay() -> u32 {
    20
}

fn default_retry_max() -> u32 {
    5
}

#[derive(Debug, Deserialize)]
struct RawRoot {
    #[serde(default = "default_server_addr")]
    server_addr: String,
    #[serde(default = "default_server_port")]
    server_port: u16,
    auth: RawAuth,
    #[serde(default)]
    transport: RawTransport,
    #[serde(default)]
    connection: RawConnection,
    #[serde(default)]
    proxies: Vec<RawProxy>,
}

#[derive(Debug, Deserialize)]
struct RawAuth {
    token: String,
    name: Option<String>,
}

#[derive(Debug, Deserialize)]
struct RawTransport {
    #[serde(default = "default_tcp")]
    protocol: String,
    #[serde(default)]
    multiplex: RawMultiplex,
    #[serde(default)]
    tls: RawTls,
}

impl Default for RawTransport {
    fn default() -> Self {
        Self {
            protocol: default_tcp(),
            multiplex: RawMultiplex::default(),
            tls: RawTls::default(),
        }
    }
}

fn default_tcp() -> String {
    "tcp".into()
}

#[derive(Debug, Deserialize)]
struct RawMultiplex {
    #[serde(default = "default_true")]
    enabled: bool,
}

impl Default for RawMultiplex {
    fn default() -> Self {
        Self { enabled: true }
    }
}

#[derive(Debug, Deserialize)]
struct RawTls {
    #[serde(default = "default_true")]
    enabled: bool,
    ca_file: Option<String>,
    cert_file: Option<String>,
    key_file: Option<String>,
}

impl Default for RawTls {
    fn default() -> Self {
        Self {
            enabled: true,
            ca_file: None,
            cert_file: None,
            key_file: None,
        }
    }
}

#[derive(Debug, Default, Deserialize)]
struct RawConnection {
    #[serde(default)]
    retry: RawRetry,
}

#[derive(Debug, Deserialize)]
struct RawRetry {
    #[serde(default = "default_retry_initial")]
    initial_delay: u32,
    #[serde(default = "default_retry_max_delay")]
    max_delay: u32,
    #[serde(default = "default_retry_max")]
    max_retries: u32,
}

impl Default for RawRetry {
    fn default() -> Self {
        Self {
            initial_delay: 1,
            max_delay: 20,
            max_retries: 5,
        }
    }
}

#[derive(Debug, Deserialize)]
struct RawProxy {
    name: String,
    protocol: String,
    #[serde(default = "default_true")]
    enabled: bool,
    #[serde(default = "default_local_ip")]
    local_ip: String,
    local_port: Option<u32>,
    remote_port: Option<u32>,
    #[serde(default = "default_true")]
    auto_domain: bool,
    #[serde(default)]
    custom_domains: Vec<String>,
    #[serde(default)]
    sub_domains: Vec<String>,
    #[serde(default)]
    targets: Vec<RawTarget>,
    #[serde(default)]
    transport: Option<RawProxyTransport>,
}

fn default_local_ip() -> String {
    "127.0.0.1".into()
}

#[derive(Debug, Deserialize)]
struct RawTarget {
    host: String,
    port: u32,
    name: Option<String>,
    weight: Option<i32>,
}

#[derive(Debug, Default, Deserialize)]
struct RawProxyTransport {
    multiplex: Option<bool>,
    encrypt: Option<bool>,
    protocol: Option<String>,
}

fn build_config(raw: RawRoot) -> Result<AppConfig, ConfigError> {
    let token = raw.auth.token.trim();
    if token.is_empty() {
        return Err(ConfigError::Invalid(
            "配置错误: [auth].token 不能为空".into(),
        ));
    }

    let protocol = raw.transport.protocol.trim().to_ascii_lowercase();
    if !crate::transport::is_supported(&protocol) {
        return Err(ConfigError::Invalid(format!(
            "配置错误: 当前支持 transport.protocol = {:?}，当前为 \"{}\"",
            crate::transport::supported_protocols(),
            raw.transport.protocol
        )));
    }
    if !raw.transport.multiplex.enabled {
        return Err(ConfigError::Invalid(
            "配置错误: 当前仅支持多路复用，请设置 [transport.multiplex] enabled = true".into(),
        ));
    }

    if raw.proxies.is_empty() {
        warn!("未配置 [[proxies]]，将以上报空列表启动");
    }

    let mut names = HashSet::new();
    let mut proxies = Vec::with_capacity(raw.proxies.len());
    for p in raw.proxies {
        let name = p.name.trim().to_string();
        if name.is_empty() {
            return Err(ConfigError::Invalid(
                "配置错误: proxies.name 不能为空".into(),
            ));
        }
        if !names.insert(name.clone()) {
            return Err(ConfigError::Invalid(format!(
                "配置错误: 代理名称重复: {name}"
            )));
        }

        let proto = match p.protocol.trim().to_ascii_lowercase().as_str() {
            "tcp" => ProxyProtocol::Tcp,
            "http" => ProxyProtocol::Http,
            other => {
                return Err(ConfigError::Invalid(format!(
                    "配置错误: 代理 [{name}] 的 protocol=\"{other}\" ，当前仅支持 tcp/http"
                )));
            }
        };

        let mut targets: Vec<TargetConfig> = p
            .targets
            .into_iter()
            .map(|t| TargetConfig {
                host: t.host,
                port: t.port,
                name: t.name,
                weight: t.weight,
            })
            .collect();
        if let Some(port) = p.local_port {
            targets.push(TargetConfig {
                host: p.local_ip.clone(),
                port,
                name: Some(name.clone()),
                weight: Some(1),
            });
        }
        if targets.is_empty() {
            return Err(ConfigError::Invalid(format!(
                "配置错误"
            )));
        }

        let transport = match p.transport {
            Some(t) => {
                if let Some(false) = t.multiplex {
                    return Err(ConfigError::Invalid(format!(
                        "配置错误: 代理 [{name}] 设置 multiplex=false，当前仅支持多路复用"
                    )));
                }
                let protocol = t.protocol.filter(|tp| tp.trim().eq_ignore_ascii_case("tcp"));
                ProxyTransportConfig {
                    multiplex: t.multiplex.or(Some(true)),
                    encrypt: t.encrypt,
                    compress: Some(false),
                    protocol,
                }
            }
            None => ProxyTransportConfig {
                multiplex: Some(true),
                encrypt: None,
                compress: None,
                protocol: None,
            },
        };

        let domain = match proto {
            ProxyProtocol::Http => Some(DomainConfig {
                auto_domain: p.auto_domain,
                custom_domains: p.custom_domains,
                sub_domains: p.sub_domains,
            }),
            ProxyProtocol::Tcp => None,
        };

        proxies.push(ProxyConfig {
            name,
            protocol: proto,
            enabled: p.enabled,
            targets,
            remote_port: if proto == ProxyProtocol::Tcp {
                p.remote_port
            } else {
                None
            },
            domain,
            transport,
        });
    }

    if !proxies.is_empty() && !proxies.iter().any(|p| p.enabled) {
        warn!("所有 [[proxies]] 均为 enabled=false");
    }

    let agent_name = raw
        .auth
        .name
        .filter(|s| !s.trim().is_empty())
        .map(|s| s.trim().to_string())
        .unwrap_or_else(|| {
            hostname::get()
                .ok()
                .and_then(|h| h.into_string().ok())
                .unwrap_or_else(|| "orbien".into())
        });

    Ok(AppConfig {
        server_addr: raw.server_addr,
        server_port: raw.server_port,
        auth: AuthConfig {
            token: token.to_string(),
            name: agent_name,
        },
        transport: TransportConfig {
            protocol,
            multiplex: true,
            tls: TlsConfig {
                enabled: raw.transport.tls.enabled,
                ca_file: raw.transport.tls.ca_file.map(PathBuf::from),
                cert_file: raw.transport.tls.cert_file.map(PathBuf::from),
                key_file: raw.transport.tls.key_file.map(PathBuf::from),
            },
        },
        retry: RetryConfig {
            initial_delay_secs: raw.connection.retry.initial_delay,
            max_delay_secs: raw.connection.retry.max_delay,
            max_retries: raw.connection.retry.max_retries,
        },
        proxies,
    })
}
