package com.xiaoniucode.etp.core.enums;

public enum DomainType {
    AUTO(0, "自动生成"),
    SUBDOMAIN(1, "子域名"),
    CUSTOM_DOMAIN(2, "自定义域名");

    DomainType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    private final Integer type;
    private final String description;
    public static DomainType fromType(Integer type) {
        for (DomainType domainType : values()) {
            if (domainType.getType().equals(type)) {
                return domainType;
            }
        }
        throw new IllegalArgumentException("Unknown domain type: " + type);
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }


}
