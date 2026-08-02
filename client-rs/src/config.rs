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
    pub quic: QuicConfig,
    pub websocket: WebSocketConfig,
}

#[derive(Debug, Clone)]
pub struct TlsConfig {
    pub enabled: bool,
    pub ca_file: Option<PathBuf>,
    pub cert_file: Option<PathBuf>,
    pub key_file: Option<PathBuf>,
}

pub const DEFAULT_QUIC_PORT: u16 = 9529;
pub const DEFAULT_WEBSOCKET_PORT: u16 = 9528;
pub const DEFAULT_WEBSOCKET_PATH: &str = "/tunnel";
pub const DEFAULT_WEBSOCKET_MAX_FRAME_SIZE: usize = 10 * 1024 * 1024;

#[derive(Debug, Clone)]
pub struct QuicConfig {
    pub port: u16,
    pub max_idle_timeout_ms: u64,
    pub initial_max_data: u64,
    pub initial_max_stream_data: u64,
    pub initial_max_streams_bidi: u32,
}

impl Default for QuicConfig {
    fn default() -> Self {
        Self {
            port: DEFAULT_QUIC_PORT,
            max_idle_timeout_ms: 120_000,
            initial_max_data: 1_048_576,
            initial_max_stream_data: 1_048_576,
            initial_max_streams_bidi: 100,
        }
    }
}

#[derive(Debug, Clone)]
pub struct WebSocketConfig {
    pub port: u16,
    pub path: String,
    pub max_frame_size: usize,
}

impl Default for WebSocketConfig {
    fn default() -> Self {
        Self {
            port: DEFAULT_WEBSOCKET_PORT,
            path: DEFAULT_WEBSOCKET_PATH.to_string(),
            max_frame_size: DEFAULT_WEBSOCKET_MAX_FRAME_SIZE,
        }
    }
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
    Https,
}

impl ProxyProtocol {
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Tcp => "tcp",
            Self::Http => "http",
            Self::Https => "https",
        }
    }

    pub fn is_http_or_https(self) -> bool {
        matches!(self, Self::Http | Self::Https)
    }
}

#[derive(Debug, Clone)]
pub struct ProxyTlsCertConfig {
    pub key_file: PathBuf,
    pub cert_file: PathBuf,
}

#[derive(Debug, Clone)]
pub struct ProxyConfig {
    pub name: String,
    pub protocol: ProxyProtocol,
    pub enabled: bool,
    pub targets: Vec<TargetConfig>,
    pub remote_port: Option<u32>,
    pub force_https: bool,
    pub domain: Option<DomainConfig>,
    pub tls_cert: Option<ProxyTlsCertConfig>,
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

fn parse_proxy_tls(
    proxy_name: &str,
    tls: Option<RawProxyTls>,
) -> Result<Option<ProxyTlsCertConfig>, ConfigError> {
    let Some(tls) = tls else {
        return Ok(None);
    };
    Ok(Some(ProxyTlsCertConfig {
        key_file: require_existing_file(proxy_name, "tls.key_file", tls.key_file)?,
        cert_file: require_existing_file(proxy_name, "tls.cert_file", tls.cert_file)?,
    }))
}

fn require_existing_file(
    proxy_name: &str,
    field: &str,
    value: Option<String>,
) -> Result<PathBuf, ConfigError> {
    let path = value
        .as_deref()
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .ok_or_else(|| {
            ConfigError::Invalid(format!("配置错误: 代理 [{proxy_name}] 请配置 {field}"))
        })?;
    let path_buf = PathBuf::from(path);
    if !path_buf.is_file() {
        return Err(ConfigError::Invalid(format!(
            "配置错误: 代理 [{proxy_name}] {field} 不存在: {path}"
        )));
    }
    Ok(path_buf)
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
    #[serde(default)]
    quic: RawQuic,
    #[serde(default)]
    websocket: RawWebsocket,
}

impl Default for RawTransport {
    fn default() -> Self {
        Self {
            protocol: default_tcp(),
            multiplex: RawMultiplex::default(),
            tls: RawTls::default(),
            quic: RawQuic::default(),
            websocket: RawWebsocket::default(),
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
        Self {
            enabled: default_true(),
        }
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
            enabled: default_true(),
            ca_file: None,
            cert_file: None,
            key_file: None,
        }
    }
}

#[derive(Debug, Deserialize)]
struct RawQuic {
    #[serde(default)]
    server_port: Option<u16>,
    #[serde(default)]
    port: Option<u16>,
    #[serde(default = "default_quic_idle")]
    max_idle_timeout_ms: u64,
    #[serde(default = "default_quic_max_data")]
    initial_max_data: u64,
    #[serde(default = "default_quic_max_data")]
    initial_max_stream_data: u64,
    #[serde(default = "default_quic_max_streams")]
    initial_max_streams_bidi: u32,
}

impl Default for RawQuic {
    fn default() -> Self {
        Self {
            server_port: None,
            port: None,
            max_idle_timeout_ms: default_quic_idle(),
            initial_max_data: default_quic_max_data(),
            initial_max_stream_data: default_quic_max_data(),
            initial_max_streams_bidi: default_quic_max_streams(),
        }
    }
}

fn default_quic_idle() -> u64 {
    120_000
}

fn default_quic_max_data() -> u64 {
    1_048_576
}

fn default_quic_max_streams() -> u32 {
    100
}

#[derive(Debug, Deserialize)]
struct RawWebsocket {
    #[serde(default)]
    server_port: Option<u16>,
    #[serde(default)]
    port: Option<u16>,
    #[serde(default)]
    path: Option<String>,
    #[serde(default)]
    max_frame_size: Option<usize>,
}

impl Default for RawWebsocket {
    fn default() -> Self {
        Self {
            server_port: None,
            port: None,
            path: None,
            max_frame_size: None,
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
            initial_delay: default_retry_initial(),
            max_delay: default_retry_max_delay(),
            max_retries: default_retry_max(),
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
    force_https: Option<bool>,
    #[serde(default = "default_true")]
    auto_domain: bool,
    #[serde(default)]
    custom_domains: Vec<String>,
    #[serde(default)]
    sub_domains: Vec<String>,
    #[serde(default)]
    targets: Vec<RawTarget>,
    #[serde(default)]
    tls: Option<RawProxyTls>,
    #[serde(default)]
    ssl: Option<RawProxyTls>,
    #[serde(default)]
    transport: Option<RawProxyTransport>,
}

#[derive(Debug, Deserialize)]
struct RawProxyTls {
    cert_file: Option<String>,
    key_file: Option<String>,
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
    compress: Option<bool>,
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

    let quic = build_quic_config(&raw.transport.quic)?;
    let websocket = build_websocket_config(&raw.transport.websocket)?;
    if protocol == "quic" {
        if let Err(e) = crate::transport::quic::validate_quic_config(&quic) {
            return Err(ConfigError::Invalid(e.to_string()));
        }
    }
    if protocol == "websocket" && !raw.transport.tls.enabled {
        return Err(ConfigError::Invalid(
            "配置错误: WebSocket 传输必须启用 TLS，请设置 [transport.tls] enabled = true".into(),
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
            "https" => ProxyProtocol::Https,
            other => {
                return Err(ConfigError::Invalid(format!(
                    "配置错误: 代理 [{name}] 的 protocol=\"{other}\" ，当前仅支持 tcp/http/https"
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
            return Err(ConfigError::Invalid(
                "配置错误: 代理目标不能为空，请配置 local_port 或 [[proxies.targets]]".into(),
            ));
        }

        // 对齐 Java TomlConfigLoader：代理 transport 与全局 transport.protocol 解耦
        // - 无 [proxies.transport]：仅带上全局 multiplex，不上报 protocol（服务端缺省 TCP）
        // - 有 [proxies.transport]：protocol 仅在显式配置时上报；encrypt 默认 true；compress 默认 false
        let transport = match p.transport {
            Some(t) => {
                let proxy_protocol = normalize_proxy_transport_protocol(t.protocol, &name)?;
                ProxyTransportConfig {
                    multiplex: Some(t.multiplex.unwrap_or(true)),
                    encrypt: Some(t.encrypt.unwrap_or(true)),
                    compress: Some(t.compress.unwrap_or(false)),
                    protocol: proxy_protocol,
                }
            }
            None => ProxyTransportConfig {
                multiplex: Some(true),
                encrypt: None,
                compress: None,
                protocol: None,
            },
        };

        let force_https = match proto {
            ProxyProtocol::Https => p.force_https.unwrap_or(true),
            _ => false,
        };

        let domain = if proto.is_http_or_https() {
            Some(DomainConfig {
                auto_domain: p.auto_domain,
                custom_domains: p.custom_domains,
                sub_domains: p.sub_domains,
            })
        } else {
            None
        };

        let tls_cert = match proto {
            ProxyProtocol::Https => parse_proxy_tls(&name, p.tls.or(p.ssl))?,
            _ => None,
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
            force_https,
            domain,
            tls_cert,
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
            quic,
            websocket,
        },
        retry: RetryConfig {
            initial_delay_secs: raw.connection.retry.initial_delay,
            max_delay_secs: raw.connection.retry.max_delay,
            max_retries: raw.connection.retry.max_retries,
        },
        proxies,
    })
}

fn build_quic_config(raw: &RawQuic) -> Result<QuicConfig, ConfigError> {
    let port = raw.server_port.or(raw.port).unwrap_or(DEFAULT_QUIC_PORT);
    if port == 0 {
        return Err(ConfigError::Invalid(
            "配置错误: [transport.quic] server_port/port 无效".into(),
        ));
    }
    Ok(QuicConfig {
        port,
        max_idle_timeout_ms: raw.max_idle_timeout_ms,
        initial_max_data: raw.initial_max_data,
        initial_max_stream_data: raw.initial_max_stream_data,
        initial_max_streams_bidi: raw.initial_max_streams_bidi,
    })
}

fn build_websocket_config(raw: &RawWebsocket) -> Result<WebSocketConfig, ConfigError> {
    let port = raw
        .server_port
        .or(raw.port)
        .unwrap_or(DEFAULT_WEBSOCKET_PORT);
    if port == 0 {
        return Err(ConfigError::Invalid(
            "配置错误: [transport.websocket] server_port/port 无效".into(),
        ));
    }
    let path = raw
        .path
        .as_deref()
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .unwrap_or(DEFAULT_WEBSOCKET_PATH)
        .to_string();
    let path = if path.starts_with('/') {
        path
    } else {
        format!("/{path}")
    };
    let max_frame_size = raw
        .max_frame_size
        .unwrap_or(DEFAULT_WEBSOCKET_MAX_FRAME_SIZE);
    if max_frame_size == 0 {
        return Err(ConfigError::Invalid(
            "配置错误: [transport.websocket] max_frame_size 必须 > 0".into(),
        ));
    }
    Ok(WebSocketConfig {
        port,
        path,
        max_frame_size,
    })
}

fn normalize_proxy_transport_protocol(
    protocol: Option<String>,
    proxy_name: &str,
) -> Result<Option<String>, ConfigError> {
    let Some(raw) = protocol else {
        return Ok(None);
    };
    let p = raw.trim().to_ascii_lowercase();
    match p.as_str() {
        "tcp" | "quic" | "websocket" => Ok(Some(p)),
        "" => Ok(None),
        other => Err(ConfigError::Invalid(format!(
            "配置错误: 代理 [{proxy_name}] transport.protocol=\"{other}\" ，当前仅支持 tcp/quic/websocket"
        ))),
    }
}
