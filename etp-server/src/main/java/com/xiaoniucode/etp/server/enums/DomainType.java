package com.xiaoniucode.etp.server.enums;

public enum DomainType {
    AUTO(0, "自动生成"),
    SUBDOMAIN(1, "子域名"),
    CUSTOM_DOMAIN(2, "自定义域名");

    DomainType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    private final int type;
    private final String description;

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
