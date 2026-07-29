package io.github.lxien.orbien.core.transport.tls;

import io.github.lxien.orbien.core.utils.StringUtils;
import io.github.lxien.orbien.core.domain.TlsConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class TlsConfigSupport {

    private TlsConfigSupport() {
    }

    public static boolean hasClientCredentials(TlsConfig tlsConfig) {
        return tlsConfig != null
                && StringUtils.hasText(tlsConfig.getCertFile())
                && StringUtils.hasText(tlsConfig.getKeyFile());
    }

    /**
     * 将 TLS 证书路径解析为相对配置文件目录的绝对路径
     */
    public static TlsConfig resolveAbsolutePaths(TlsConfig tlsConfig, Path configDir) {
        if (tlsConfig == null || configDir == null) {
            return tlsConfig;
        }
        return new TlsConfig(
                tlsConfig.isEnabled(),
                resolvePath(configDir, tlsConfig.getCertFile()),
                resolvePath(configDir, tlsConfig.getKeyFile()),
                resolvePath(configDir, tlsConfig.getCaFile()),
                tlsConfig.getKeyPassword()
        );
    }

    public static String resolveAbsolutePath(Path configDir, String path) {
        return resolvePath(configDir, path);
    }

    private static String resolvePath(Path configDir, String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        Path resolved = Paths.get(path);
        if (resolved.isAbsolute()) {
            return path;
        }
        return configDir.resolve(resolved).normalize().toString();
    }
}