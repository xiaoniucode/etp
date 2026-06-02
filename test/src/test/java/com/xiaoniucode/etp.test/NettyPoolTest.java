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

package com.xiaoniucode.etp.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocatorMetric;

import java.util.ArrayList;
import java.util.List;

public class NettyPoolTest {

    public static void main(String[] args) throws Exception {

        List<ByteBuf> list = new ArrayList<>();

        while (true) {

            for (int i = 0; i < 1000; i++) {

                ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(1024);

                list.add(buf);
            }

            System.out.println(
                    "UsedDirectMemory = "
                            + human(
                            PooledByteBufAllocator.DEFAULT.metric().usedDirectMemory()
                    )
            );
            PooledByteBufAllocatorMetric metric = PooledByteBufAllocator.DEFAULT.metric();
            long size = metric.usedDirectMemory();

            Thread.sleep(1000);
        }
    }

    private static String human(long bytes) {

        double value = bytes;

        String[] units = {"B", "KB", "MB", "GB"};

        int unit = 0;

        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }

        return String.format("%.2f %s", value, units[unit]);
    }
}