package com.xiaoniucode.etp.autoconfigure;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
@Data
public class AccessControlProperties  implements Serializable {
    @Setter
    private boolean enable = false;
    @Setter
    @NestedConfigurationProperty
    private AccessControlMode mode = AccessControlMode.ALLOW;
    private final Set<String> allow = new HashSet<>();
    private final Set<String> deny = new HashSet<>();

    /**
     * IP 访问控制模式
     */
    @Getter
    enum AccessControlMode {
        /**
         * 白名单模式：只允许指定 IP 访问
         */
        ALLOW,
        /**
         * 黑名单模式：拒绝指定 IP 访问，允许其他 IP 访问
         */
        DENY
    }
}
