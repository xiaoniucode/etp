/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.core.transport.compress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * SnappyCompressor 压缩/解压一致性测试
 */
public class SnappyCompressorConsistencyTest {

    public static void main(String[] args) {
        SnappyCompressor compressor = new SnappyCompressor();
        EmbeddedChannel channel = new EmbeddedChannel();

        System.out.println("=== SnappyCompressor 压缩解压一致性测试开始 ===\n");

        // 测试用例1：小数据（< 1024字节，不压缩）
        testConsistency(compressor, channel, "小数据测试 - Hello Snappy! 测试中文支持。".getBytes(StandardCharsets.UTF_8), "小数据");

        // 测试用例2：中等数据
        testConsistency(compressor, channel, generateData(5000), "中等数据 (5KB)");

        // 测试用例3：接近你之前的数据量（约1MB）
        testConsistency(compressor, channel, generateData(998890), "大数据 (≈999KB)");

        // 测试用例4：随机二进制数据（更严格测试）
        testConsistency(compressor, channel, generateRandomData(200_000), "随机二进制数据 (200KB)");

        // 测试用例5：极大数据（可选，约3MB）
        // testConsistency(compressor, channel, generateData(3_000_000), "极大数据 (3MB)");

        System.out.println("\n=== 所有测试完成 ===");
        channel.finishAndReleaseAll();
    }

    /**
     * 核心测试方法：压缩 → 收集所有chunk → 合并 → 解压 → 对比
     */
    private static void testConsistency(SnappyCompressor compressor, EmbeddedChannel channel,
                                        byte[] originalData, String testName) {

        System.out.println("【测试】" + testName + " - 原始大小: " + originalData.length + " 字节");

        ByteBuf original = Unpooled.wrappedBuffer(originalData);
        List<ByteBuf> compressedChunks = new ArrayList<>();

        try {
            // 1. 执行压缩（会自动分块）
            compressor.compress(channel, original, compressedChunks::add, 0);

            int chunkCount = compressedChunks.size();
            System.out.println("   压缩后拆分成 " + chunkCount + " 个 chunk");

            // 2. 合并所有压缩块（模拟网络接收）
            ByteBuf allCompressed = Unpooled.buffer();
            for (ByteBuf chunk : compressedChunks) {
                allCompressed.writeBytes(chunk);
            }

            // 3. 执行解压
            ByteBuf decompressed = compressor.decompress(channel, allCompressed);

            // 4. 读取解压结果
            byte[] result = new byte[decompressed.readableBytes()];
            decompressed.readBytes(result);

            // 5. 对比结果
            boolean isConsistent = Arrays.equals(originalData, result);

            System.out.println("   解压后大小: " + result.length + " 字节");
            System.out.println("   数据一致性: " + (isConsistent ? "✅ 通过" : "❌ 失败"));

            if (!isConsistent) {
                System.out.println("   【错误】压缩解压后数据不一致！");
            }

            System.out.println("   ------------------------");

        } catch (Exception e) {
            System.out.println("   【异常】测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 释放资源
            original.release();
            compressedChunks.forEach(ByteBuf::release);
        }
    }

    /** 生成重复文本数据 */
    private static byte[] generateData(int size) {
        StringBuilder sb = new StringBuilder();
        String template = "这是测试数据片段: Hello Snappy Compression! 测试中文支持。abcdefghijklmnopqrstuvwxyz0123456789\n";
        while (sb.length() < size) {
            sb.append(template);
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(data, size); // 精确控制大小
    }

    /** 生成随机二进制数据（更严格测试） */
    private static byte[] generateRandomData(int size) {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);   // 使用固定种子保证可重复
        return data;
    }
}