package com.xiaoniucode.etp.core.enums;

/**
 * 负载均衡策略枚举
 */
public enum LoadBalanceStrategy {

    /**
     * 轮询 - 按顺序轮流分配
     */
    ROUND_ROBIN("roundrobin", "轮询"),

    /**
     * 加权轮询 - 按权重比例分配
     */
    WEIGHT("weight", "加权轮询"),

    /**
     * 随机
     */
    RANDOM("random", "随机"),

    /**
     * 最少连接 - 分配给当前连接数最少的后端
     */
    LEAST_CONN("leastconn", "最少连接");

    /**
     * 策略代码
     */
    private final String code;

    /**
     * 策略描述
     */
    private final String description;

    LoadBalanceStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static LoadBalanceStrategy fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("策略不能为空");
        }
        for (LoadBalanceStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("未知策略: " + code);
    }

    /**
     * 判断是否需要权重配置
     */
    public boolean requireWeight() {
        return this == WEIGHT;
    }

    /**
     * 判断是否需要连接计数
     */
    public boolean requireConnectionCount() {
        return this == LEAST_CONN;
    }

    @Override
    public String toString() {
        return code + "(" + description + ")";
    }
}