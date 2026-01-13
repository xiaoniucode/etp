package com.xiaoniucode.etp.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * 压缩工具类，使用Snappy算法
 */
public class CompressionUtil {
    // 压缩阈值：超过此长度才进行压缩（单位：字节）
    public static final int COMPRESSION_THRESHOLD = 4;
    
    /**
     * 压缩ByteBuf数据（如果超过阈值）
     */
    public static ByteBuf compress(ByteBuf source, boolean autoRelease) {
        if (source == null || !source.isReadable()) {
            if (autoRelease && source != null) {
                source.release();
            }
            return Unpooled.EMPTY_BUFFER;
        }
        
        // 未达到压缩阈值，直接返回原ByteBuf
        if (source.readableBytes() < COMPRESSION_THRESHOLD) {
            return source;
        }
        
        try {
            // 读取原始数据
            byte[] data = new byte[source.readableBytes()];
            source.readBytes(data);
            
            // 压缩数据
            byte[] compressedData = Snappy.compress(data);
            
            // 创建压缩后的ByteBuf
            ByteBuf result = Unpooled.wrappedBuffer(compressedData);
            
            // 自动释放原始ByteBuf
            if (autoRelease) {
                source.release();
            }
            
            return result;
        } catch (IOException e) {
            // 压缩失败，返回原始数据
            if (autoRelease) {
                source.release();
            }
            return source;
        }
    }
    
    /**
     * 解压缩ByteBuf数据
     */
    public static ByteBuf decompress(ByteBuf source, boolean autoRelease) {
        if (source == null || !source.isReadable()) {
            if (autoRelease && source != null) {
                source.release();
            }
            return Unpooled.EMPTY_BUFFER;
        }
        
        try {
            // 读取压缩数据
            byte[] data = new byte[source.readableBytes()];
            source.readBytes(data);
            
            // 解压缩数据
            byte[] decompressedData = Snappy.uncompress(data);
            
            // 创建解压缩后的ByteBuf
            ByteBuf result = Unpooled.wrappedBuffer(decompressedData);
            
            // 自动释放原始ByteBuf
            if (autoRelease) {
                source.release();
            }
            
            return result;
        } catch (IOException e) {
            // 解压失败，返回原始数据
            if (autoRelease) {
                source.release();
            }
            return source;
        }
    }
}