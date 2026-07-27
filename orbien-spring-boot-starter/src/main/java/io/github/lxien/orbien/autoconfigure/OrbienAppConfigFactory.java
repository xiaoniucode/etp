package io.github.lxien.orbien.autoconfigure;

import io.github.lxien.orbien.client.config.AppConfig;
import io.github.lxien.orbien.client.config.DefaultAppConfig;
import io.github.lxien.orbien.client.config.domain.AuthConfig;
import io.github.lxien.orbien.client.config.domain.ConnectionConfig;
import io.github.lxien.orbien.client.config.domain.MultiplexConfig;
import io.github.lxien.orbien.client.config.domain.PoolConfig;
import io.github.lxien.orbien.client.config.domain.RetryConfig;
import io.github.lxien.orbien.client.config.domain.TransportConfig;
import io.github.lxien.orbien.core.domain.AccessControlConfig;
import io.github.lxien.orbien.core.domain.BasicAuthConfig;
import io.github.lxien.orbien.core.domain.BandwidthConfig;
import io.github.lxien.orbien.core.domain.HttpUser;
import io.github.lxien.orbien.core.domain.ProxyConfig;
import io.github.lxien.orbien.core.domain.RouteConfig;
import io.github.lxien.orbien.core.domain.Target;
import io.github.lxien.orbien.core.domain.TimeAccessConfig;
import io.github.lxien.orbien.core.domain.TimeAccessWindow;
import io.github.lxien.orbien.core.domain.TlsConfig;
import io.github.lxien.orbien.core.domain.TransportCustomConfig;
import io.github.lxien.orbien.core.enums.AccessControl;
import io.github.lxien.orbien.core.enums.AgentType;
import io.github.lxien.orbien.core.enums.ProxyStatus;
import io.github.lxien.orbien.core.enums.TransportProtocol;
import io.github.lxien.orbien.core.time.TimeAccessSupport;
import io.github.lxien.orbien.core.transport.compress.CompressionType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class OrbienAppConfigFactory {

    private OrbienAppConfigFactory() {
    }

    static AppConfig build(OrbienClientProperties properties,
                           ResourceLoader resourceLoader,
                           String appName,
                           int localPort) {
        return DefaultAppConfig.builder()
                .serverAddr(properties.getServerAddr())
                .serverPort(properties.getServerPort())
                .agentType(AgentType.SESSION)
                .authConfig(buildAuthConfig(properties))
                .transportConfig(buildTransportConfig(properties, resourceLoader))
                .connectionConfig(buildConnectionConfig(properties))
                .addProxy(buildProxyConfig(properties, appName, localPort))
                .build();
    }

    private static AuthConfig buildAuthConfig(OrbienClientProperties properties) {
        AuthConfig authConfig = new AuthConfig();
        authConfig.setToken(properties.getAuth().getToken().trim());
        if (StringUtils.hasText(properties.getAuth().getName())) {
            authConfig.setName(properties.getAuth().getName().trim());
        }
        return authConfig;
    }

    private static TransportConfig buildTransportConfig(OrbienClientProperties properties,
                                                        ResourceLoader resourceLoader) {
        TransportProperties transportProps = properties.getTransport();

        TransportConfig transportConfig = new TransportConfig();
        TransportProtocol controlProtocol = transportProps.getProtocol() != null
                ? transportProps.getProtocol()
                : TransportProtocol.TCP;
        transportConfig.setProtocol(controlProtocol);
        transportConfig.setMultiplexConfig(new MultiplexConfig(transportProps.getMultiplex().isEnabled()));
        transportConfig.setTlsConfig(buildTlsConfig(transportProps.getTls(), resourceLoader));

        if (transportProps.getWebsocket().getServerPort() != null) {
            transportConfig.getWebsocket().setPort(transportProps.getWebsocket().getServerPort());
        }
        if (StringUtils.hasText(transportProps.getWebsocket().getPath())) {
            transportConfig.getWebsocket().setPath(transportProps.getWebsocket().getPath());
        }
        if (transportProps.getQuic().getServerPort() != null) {
            transportConfig.getQuic().setPort(transportProps.getQuic().getServerPort());
        }
        return transportConfig;
    }

    private static TlsConfig buildTlsConfig(TransportProperties.TlsProperties tlsProps,
                                            ResourceLoader resourceLoader) {
        boolean enabled = tlsProps.getEnabled() == null || tlsProps.getEnabled();
        TlsConfig tlsConfig = new TlsConfig(enabled);
        tlsConfig.setCertFile(resolveTlsPath(tlsProps.getCertFile(), resourceLoader));
        tlsConfig.setKeyFile(resolveTlsPath(tlsProps.getKeyFile(), resourceLoader));
        tlsConfig.setCaFile(resolveTlsPath(tlsProps.getCaFile(), resourceLoader));
        tlsConfig.setKeyPassword(tlsProps.getKeyPassword());
        return tlsConfig;
    }

    private static ConnectionConfig buildConnectionConfig(OrbienClientProperties properties) {
        ConnectionProperties connectionProps = properties.getConnection();
        ConnectionConfig connectionConfig = new ConnectionConfig();

        ConnectionProperties.RetryProperties retryProps = connectionProps.getRetry();
        RetryConfig retryConfig = new RetryConfig();
        if (retryProps.getInitialDelay() != null) {
            retryConfig.setInitialDelay(retryProps.getInitialDelay());
        }
        if (retryProps.getMaxDelay() != null) {
            retryConfig.setMaxDelay(retryProps.getMaxDelay());
        }
        if (retryProps.getMaxRetries() != null) {
            retryConfig.setMaxRetries(retryProps.getMaxRetries());
        }
        connectionConfig.setRetryConfig(retryConfig);

        ConnectionProperties.PoolProperties poolProps = connectionProps.getPool();
        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setEnabled(poolProps.isEnabled());
        poolConfig.getMultiplex().setPlain(poolProps.getMultiplex().isPlain());
        poolConfig.getMultiplex().setEncrypt(poolProps.getMultiplex().isEncrypt());
        poolConfig.getDirect().setPlainCount(poolProps.getDirect().getPlainCount());
        poolConfig.getDirect().setEncryptCount(poolProps.getDirect().getEncryptCount());
        connectionConfig.setPoolConfig(poolConfig);
        return connectionConfig;
    }

    private static ProxyConfig buildProxyConfig(OrbienClientProperties properties,
                                                String appName,
                                                int localPort) {
        ProxyProperties proxy = properties.getProxy();
        ProxyProtocol protocol = proxy.getProtocol();

        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setName(appName);
        proxyConfig.setProtocol(protocol.toProtocolType());
        proxyConfig.setRemotePort(proxy.getRemotePort());
        proxyConfig.setStatus(ProxyStatus.OPEN);
        proxyConfig.addTarget(new Target(proxy.getLocalIp(), localPort, 1, "local"));

        // HTTPS：未配置时默认开启强制跳转，与核心 ForceHttpsPolicy 一致
        if (protocol == ProxyProtocol.HTTPS) {
            proxyConfig.setForceHttps(proxy.getForceHttps() == null || proxy.getForceHttps());
        } else {
            proxyConfig.setForceHttps(false);
        }

        AccessControlProperties accessControl = proxy.getAccessControl();
        AccessControl mode = accessControl.getMode() != null
                ? accessControl.getMode()
                : AccessControl.ALLOW;
        proxyConfig.setAccessControl(new AccessControlConfig(
                accessControl.isEnabled(),
                mode,
                accessControl.getAllow(),
                accessControl.getDeny()
        ));

        TimeAccessProperties timeAccess = proxy.getTimeAccess();
        if (timeAccess != null && timeAccess.isEnabled()) {
            List<TimeAccessWindow> windows = timeAccess.getWindows() == null
                    ? List.of()
                    : timeAccess.getWindows().stream()
                    .map(w -> new TimeAccessWindow(w.getStart(), w.getEnd()))
                    .collect(Collectors.toList());
            TimeAccessConfig timeAccessConfig = new TimeAccessConfig(
                    true,
                    timeAccess.getMode() != null ? timeAccess.getMode() : AccessControl.ALLOW,
                    timeAccess.isTimeEnabled(),
                    timeAccess.getTimezone(),
                    timeAccess.getDays(),
                    windows
            );
            TimeAccessSupport.validateConfig(timeAccessConfig);
            proxyConfig.setTimeAccess(timeAccessConfig);
        }

        BandwidthProperties bandwidth = proxy.getBandwidth();
        if (StringUtils.hasText(bandwidth.getLimitTotal())
                || StringUtils.hasText(bandwidth.getLimitIn())
                || StringUtils.hasText(bandwidth.getLimitOut())) {
            proxyConfig.setBandwidth(new BandwidthConfig(
                    bandwidth.getLimitTotal(),
                    bandwidth.getLimitIn(),
                    bandwidth.getLimitOut()
            ));
        }

        if (protocol.isHttpOrHttps()) {
            RouteConfig routeConfig = new RouteConfig();
            routeConfig.setAutoDomain(proxy.getAutoDomain() == null || proxy.getAutoDomain());
            routeConfig.getCustomDomains().addAll(proxy.getCustomDomains());
            routeConfig.getSubDomains().addAll(proxy.getSubDomains());
            proxyConfig.setRouteConfig(routeConfig);

            BasicAuthProperties basicAuth = proxy.getBasicAuth();
            if (basicAuth.isEnabled() && !basicAuth.getUsers().isEmpty()) {
                Set<HttpUser> users = basicAuth.getUsers().stream()
                        .map(user -> new HttpUser(user.getUser(), user.getPass()))
                        .collect(Collectors.toSet());
                BasicAuthConfig basicAuthConfig = new BasicAuthConfig();
                basicAuthConfig.setEnabled(true);
                basicAuthConfig.addUsers(users);
                proxyConfig.setBasicAuth(basicAuthConfig);
            }
        }

        TransportCustomProperties transport = proxy.getTransport();
        TransportCustomConfig.TransportCustomConfigBuilder transportBuilder = TransportCustomConfig.builder()
                .multiplex(transport.isMultiplex())
                .encrypt(transport.isEncrypt())
                .compress(transport.isCompress());
        if (StringUtils.hasText(transport.getCompressAlgorithm())) {
            transportBuilder.compressAlgorithm(CompressionType.of(transport.getCompressAlgorithm()));
        }
        if (transport.getProtocol() != null) {
            transportBuilder.protocol(transport.getProtocol());
        }
        proxyConfig.setTransport(transportBuilder.build());
        return proxyConfig;
    }

    /**
     * 将 Spring Resource 位置解析为本地文件路径；JAR 内资源会复制到临时文件
     */
    private static String resolveTlsPath(String location, ResourceLoader resourceLoader) {
        if (!StringUtils.hasText(location)) {
            return null;
        }
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("TLS 证书资源不存在: " + location);
            }
            if (resource.isFile()) {
                return resource.getFile().getAbsolutePath();
            }

            String filename = resource.getFilename();
            String suffix = "";
            if (filename != null && filename.contains(".")) {
                suffix = filename.substring(filename.lastIndexOf('.'));
            }
            Path tempFile = Files.createTempFile("orbien-tls-", suffix);
            tempFile.toFile().deleteOnExit();
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalStateException("无法加载 TLS 证书文件: " + location, e);
        }
    }
}
