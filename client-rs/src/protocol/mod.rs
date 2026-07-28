//! `magic | version | msgType | streamId | flags | length | payload`
mod codec;
mod types;

pub use codec::{Codec, DecodeError};
pub use types::*;

/// 协议魔数: `TMSP`
pub const MAGIC: u32 = 0x544D_5350;
/// 协议版本号
pub const VERSION: u8 = 0x10;
/// 默认允许的最大帧长度
pub const DEFAULT_MAX_FRAME_LENGTH: usize = 10 * 1024 * 1024;
/// 固定头部长度
pub const HEADER_LEN: usize = 15;
