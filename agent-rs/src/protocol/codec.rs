use bytes::{Buf, BufMut, BytesMut};
use tokio_util::codec::{Decoder, Encoder};

use super::types::Frame;
use super::{DEFAULT_MAX_FRAME_LENGTH, HEADER_LEN, MAGIC};

#[derive(Debug, thiserror::Error)]
pub enum DecodeError {
    #[error("无效魔数: {0:#x}")]
    BadMagic(u32),
    #[error("帧过长: {length} > {max}")]
    FrameTooLarge { length: usize, max: usize },
    #[error("IO 错误: {0}")]
    Io(#[from] std::io::Error),
}

pub struct Codec {
    max_frame_length: usize,
}

impl Default for Codec {
    fn default() -> Self {
        Self::new(DEFAULT_MAX_FRAME_LENGTH)
    }
}

impl Codec {
    pub fn new(max_frame_length: usize) -> Self {
        Self { max_frame_length }
    }
}

impl Decoder for Codec {
    type Item = Frame;
    type Error = DecodeError;

    fn decode(&mut self, src: &mut BytesMut) -> Result<Option<Self::Item>, Self::Error> {
        if src.len() < HEADER_LEN {
            return Ok(None);
        }

        // payload 长度字段位于偏移 11，大端 u32
        let payload_len = {
            let mut cursor = &src[11..15];
            cursor.get_u32() as usize
        };
        let frame_len = HEADER_LEN + payload_len;
        if frame_len > self.max_frame_length {
            return Err(DecodeError::FrameTooLarge {
                length: frame_len,
                max: self.max_frame_length,
            });
        }
        if src.len() < frame_len {
            return Ok(None);
        }

        let mut frame_buf = src.split_to(frame_len);
        let magic = frame_buf.get_u32();
        if magic != MAGIC {
            return Err(DecodeError::BadMagic(magic));
        }
        let version = frame_buf.get_u8();
        let msg_type = frame_buf.get_u8();
        let stream_id = frame_buf.get_u32();
        let flags = frame_buf.get_u8();
        let _length = frame_buf.get_u32();
        let payload = frame_buf.freeze();

        Ok(Some(Frame {
            magic,
            version,
            msg_type,
            stream_id,
            flags,
            payload,
        }))
    }
}

impl Encoder<Frame> for Codec {
    type Error = std::io::Error;

    fn encode(&mut self, item: Frame, dst: &mut BytesMut) -> Result<(), Self::Error> {
        let payload_len = item.payload.len();
        dst.reserve(HEADER_LEN + payload_len);
        dst.put_u32(item.magic);
        dst.put_u8(item.version);
        dst.put_u8(item.msg_type);
        dst.put_u32(item.stream_id);
        dst.put_u8(item.flags);
        dst.put_u32(payload_len as u32);
        dst.extend_from_slice(&item.payload);
        Ok(())
    }
}
