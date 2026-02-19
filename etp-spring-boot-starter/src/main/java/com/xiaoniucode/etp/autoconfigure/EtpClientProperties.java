package com.xiaoniucode.etp.autoconfigure;

import com.xiaoniucode.etp.client.config.domain.AuthConfig;
import com.xiaoniucode.etp.client.config.domain.RetryConfig;
import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.TlsConfig;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.core.enums.ProtocolType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * etp客户端配置属性
 *
 * @author liuxin
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "etp.client")
public class EtpClientProperties {
    /**
     * 是否启用ETP代理
     */
    private boolean enable = false;

    /**
     * 代理服务地址
     */
    private String serverAddr = "127.0.0.1";

    /**
     * 代理服务端口
     */
    private Integer serverPort = 9527;

    @NestedConfigurationProperty
    private Auth auth = new Auth();
    /**
     * TLS 加密
     */
    @NestedConfigurationProperty
    private TlsConfig tls = new TlsConfig();
    /**
     * 访问控制
     */
    @NestedConfigurationProperty
    private AccessControl accessControl = new AccessControl();
    /**
     * 公网端口
     */
    private Integer remotePort;
    /**
     * 内网IP
     */
    private String localIp = "127.0.0.1";
    /**
     * 协议
     */
    private ProtocolType protocol = ProtocolType.HTTP;
    /**
     * 自定义域名列表
     */
    private List<String> customDomains = new ArrayList<>();

    /**
     * 是否自动生成域名，默认自动生成子域名
     */
    private Boolean autoDomain = true;

    /**
     * 子域名列表
     */
    private List<String> subDomain = new ArrayList<>();


    public AuthConfig getAuthConfig() {
        AuthConfig authConfig = new AuthConfig();
        if (auth != null && StringUtils.hasText(auth.getToken())) {
            authConfig.setToken(auth.getToken());
        }
        if (auth != null && auth.getRetry() != null) {
            RetryConfig retryConfig = new RetryConfig();
            Auth.Retry retry = auth.getRetry();
            if (retry.getMaxRetries() != null) {
                retryConfig.setMaxRetries(retry.getMaxRetries());
            }
            if (retry.getMaxDelay() != null) {
                retryConfig.setMaxDelay(retry.getMaxDelay());
            }
            if (retry.getInitialDelay() != null) {
                retryConfig.setInitialDelay(retry.getInitialDelay());
            }

            authConfig.setRetry(retryConfig);
        }
        return authConfig;
    }

    public AccessControlConfig getAccessControlConfig() {
        if (accessControl != null) {
            return new AccessControlConfig(
                    accessControl.enable,
                    AccessControlMode.fromValue(accessControl.mode.name()),
                    accessControl.allow,
                    accessControl.deny);
        }
        return null;
    }

    @Getter
    static class AccessControl implements Serializable {
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

    @Getter
    @Setter
    static class Auth {
        private String token;
        /**
         * 重试配置
         */
        @NestedConfigurationProperty
        private Retry retry = new Retry();

        /**
         * 重试配置
         *
         * @author liuxin
         */
        @Getter
        @Setter
        static class Retry {
            /**
             * 初始重试延迟（秒）
             */
            private Integer initialDelay = 1;
            /**
             * 最大延迟时间（秒）
             */
            private Integer maxDelay = 20;
            /**
             * 最大重试次数
             */
            private Integer maxRetries = 5;
        }
    }
}
