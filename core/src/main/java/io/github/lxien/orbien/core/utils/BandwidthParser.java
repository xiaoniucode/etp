/*
 *    Copyright 2026 lxien
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

package io.github.lxien.orbien.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 带宽限流解析
 */
public final class BandwidthParser {

    public static final long BPS_PER_MBPS = 1_000_000L;

    /**
     * 严格匹配：正整数 + Mbps（不允许小数、前导 0、空格，单位大小写固定）
     */
    private static final Pattern STRICT_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)Mbps$");

    private BandwidthParser() {
    }

    /**
     * 将配置字符串解析为 bps
     *
     * @param value 如 {@code 10Mbps}
     * @return bps；空串返回 null
     */
    public static Long parseToBps(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        Matcher matcher = STRICT_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "带宽格式无效: " + value + "（必须为: 10Mbps，整数，无空格，单位固定为 Mbps）"
            );
        }

        long mbps;
        try {
            mbps = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值过大: " + value);
        }

        return mbpsToBps(mbps);
    }

    public static long mbpsToBps(long mbps) {
        if (mbps < 0) {
            throw new IllegalArgumentException("带宽值不能为负");
        }
        if (mbps > Long.MAX_VALUE / BPS_PER_MBPS) {
            throw new IllegalArgumentException("带宽值过大导致溢出: " + mbps + "Mbps");
        }
        return mbps * BPS_PER_MBPS;
    }

    /**
     * Mbps -> bps；null 或 ≤0 视为未配置
     */
    public static Long mbpsToBpsOrNull(Integer mbps) {
        if (mbps == null || mbps <= 0) {
            return null;
        }
        return mbpsToBps(mbps.longValue());
    }

    /**
     * bps -> Mbps（向下取整）
     */
    public static Integer bpsToMbps(Long bps) {
        if (bps == null) {
            return null;
        }
        return (int) (bps / BPS_PER_MBPS);
    }

    /**
     * bps -> 配置字符串（如 {@code 10Mbps}）
     */
    public static String formatMbps(Long bps) {
        if (bps == null) {
            return null;
        }
        return (bps / BPS_PER_MBPS) + "Mbps";
    }
}
