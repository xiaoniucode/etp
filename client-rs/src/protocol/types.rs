use bytes::{Buf, Bytes};
use thiserror::Error;

use super::{MAGIC, VERSION};

#[derive(Debug, Clone)]
pub struct Frame {
    pub magic: u32,
    pub version: u8,
    pub msg_type: u8,
    pub stream_id: u32,
    pub flags: u8,
    pub payload: Bytes,
}

impl Frame {
    pub fn new(stream_id: u32, msg_type: u8, payload: impl Into<Bytes>) -> Self {
        Self {
            magic: MAGIC,
            version: VERSION,
            msg_type,
            stream_id,
            flags: 0,
            payload: payload.into(),
        }
    }

    pub fn empty(stream_id: u32, msg_type: u8) -> Self {
        Self::new(stream_id, msg_type, Bytes::new())
    }

    pub fn with_flags(mut self, flags: u8) -> Self {
        self.flags = flags;
        self
    }
}

pub const FLAG_ENCRYPTED: u8 = 0x02;
pub const FLAG_MUX: u8 = 0x04;
pub const FLAG_DATAGRAM: u8 = 0x08;

pub fn build_flags(mux: bool, encrypted: bool, datagram: bool) -> u8 {
    let mut flags = 0u8;
    if mux {
        flags |= FLAG_MUX;
    }
    if encrypted {
        flags |= FLAG_ENCRYPTED;
    }
    if datagram {
        flags |= FLAG_DATAGRAM;
    }
    flags
}

pub fn is_encrypted(flags: u8) -> bool {
    flags & FLAG_ENCRYPTED != 0
}

pub fn is_mux(flags: u8) -> bool {
    flags & FLAG_MUX != 0
}

pub fn is_datagram(flags: u8) -> bool {
    flags & FLAG_DATAGRAM != 0
}

pub const MSG_AUTH: u8 = 0x01;
pub const MSG_AUTH_RESP: u8 = 0x02;
pub const MSG_PING: u8 = 0x03;
pub const MSG_PONG: u8 = 0x04;
pub const MSG_GOAWAY: u8 = 0x05;
pub const MSG_ERROR: u8 = 0x06;

pub const MSG_CONNECTION_CREATE: u8 = 0x07;
pub const MSG_CONNECTION_CREATE_RESP: u8 = 0x08;

pub const MSG_SERVICE_HEALTH_REPORT: u8 = 0x09;
pub const MSG_PROXY_REPORT_REQ: u8 = 0x10;
pub const MSG_PROXY_REPORT_RESP: u8 = 0x11;
pub const MSG_CONFIG_SYNC: u8 = 0x12;

pub const MSG_STREAM_OPEN: u8 = 0x20;
pub const MSG_STREAM_OPEN_RESP: u8 = 0x21;
pub const MSG_STREAM_CLOSE: u8 = 0x22;
pub const MSG_STREAM_RESET: u8 = 0x23;
pub const MSG_STREAM_DATA: u8 = 0x24;
pub const MSG_STREAM_PAUSE: u8 = 0x25;
pub const MSG_STREAM_RESUME: u8 = 0x26;

pub fn opcode_name(opcode: u8) -> &'static str {
    match opcode {
        MSG_AUTH => "AUTH",
        MSG_AUTH_RESP => "AUTH_RESP",
        MSG_PING => "PING",
        MSG_PONG => "PONG",
        MSG_GOAWAY => "GOAWAY",
        MSG_ERROR => "ERROR",
        MSG_CONNECTION_CREATE => "CONNECTION_CREATE",
        MSG_CONNECTION_CREATE_RESP => "CONNECTION_CREATE_RESP",
        MSG_SERVICE_HEALTH_REPORT => "SERVICE_HEALTH_REPORT",
        MSG_PROXY_REPORT_REQ => "PROXY_REPORT_REQ",
        MSG_PROXY_REPORT_RESP => "PROXY_REPORT_RESP",
        MSG_CONFIG_SYNC => "CONFIG_SYNC",
        MSG_STREAM_OPEN => "STREAM_OPEN",
        MSG_STREAM_OPEN_RESP => "STREAM_OPEN_RESP",
        MSG_STREAM_CLOSE => "STREAM_CLOSE",
        MSG_STREAM_RESET => "STREAM_RESET",
        MSG_STREAM_DATA => "STREAM_DATA",
        MSG_STREAM_PAUSE => "STREAM_PAUSE",
        MSG_STREAM_RESUME => "STREAM_RESUME",
        _ => "UNKNOWN",
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum TunnelTransport {
    Tcp = 0,
    Websocket = 1,
    Quic = 2,
}

impl TunnelTransport {
    pub fn from_wire(value: u8) -> Self {
        match value {
            1 => Self::Websocket,
            2 => Self::Quic,
            _ => Self::Tcp,
        }
    }

    pub fn as_str(self) -> &'static str {
        match self {
            Self::Tcp => "tcp",
            Self::Websocket => "websocket",
            Self::Quic => "quic",
        }
    }

    pub fn is_supported(self) -> bool {
        matches!(self, Self::Tcp | Self::Quic)
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct NewStreamInfo {
    pub host: String,
    pub port: u16,
    pub transport: TunnelTransport,
}

#[derive(Debug, Error)]
pub enum NewStreamError {
    #[error("目标地址不完整")]
    Incomplete,
    #[error("目标地址为空")]
    EmptyHost,
    #[error("目标地址不是合法 UTF-8")]
    InvalidUtf8,
}

pub fn decode_new_stream(mut buf: Bytes) -> Result<NewStreamInfo, NewStreamError> {
    if buf.remaining() < 2 {
        return Err(NewStreamError::Incomplete);
    }
    let host_len = buf.get_u16() as usize;
    if host_len == 0 {
        return Err(NewStreamError::EmptyHost);
    }
    if buf.remaining() < host_len + 3 {
        return Err(NewStreamError::Incomplete);
    }
    let host_bytes = buf.copy_to_bytes(host_len);
    let host = std::str::from_utf8(&host_bytes)
        .map_err(|_| NewStreamError::InvalidUtf8)?
        .to_string();
    let port = buf.get_u16();
    let transport = TunnelTransport::from_wire(buf.get_u8());
    Ok(NewStreamInfo {
        host,
        port,
        transport,
    })
}
