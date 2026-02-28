package com.xiaoniucode.etp.core.enums;

public enum DomainType {
    AUTO(0, "自动生成"),
    SUBDOMAIN(1, "子域名"),
    CUSTOM_DOMAIN(2, "自定义域名");

    DomainType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final Integer code;
    private final String description;
    public static DomainType fromType(Integer type) {
        for (DomainType domainType : values()) {
            if (domainType.getCode().equals(type)) {
                return domainType;
            }
        }
        throw new IllegalArgumentException("Unknown domain type: " + type);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
