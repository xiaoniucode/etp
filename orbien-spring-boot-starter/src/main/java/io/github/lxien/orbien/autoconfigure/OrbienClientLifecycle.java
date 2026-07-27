package io.github.lxien.orbien.autoconfigure;

import io.github.lxien.orbien.client.TunnelClient;
import io.github.lxien.orbien.client.config.AppConfig;
import io.github.lxien.orbien.client.console.SessionConsole;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class OrbienClientLifecycle implements SmartLifecycle {

    private static final int LIFECYCLE_PHASE = Integer.MAX_VALUE - 100;

    private final Environment environment;
    private final OrbienClientProperties properties;
    private final OrbienLocalPortLocator portLocator;
    private final ResourceLoader resourceLoader;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile TunnelClient tunnelClient;
    private volatile SessionConsole sessionConsole;

    public OrbienClientLifecycle(Environment environment,
                                 OrbienClientProperties properties,
                                 OrbienLocalPortLocator portLocator,
                                 ResourceLoader resourceLoader) {
        this.environment = environment;
        this.properties = properties;
        this.portLocator = portLocator;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            validateProperties();
            int localPort = portLocator.resolveRequired();
            String appName = environment.getProperty("spring.application.name", "spring-app");
            AppConfig config = OrbienAppConfigFactory.build(properties, resourceLoader, appName, localPort);

            TunnelClient client = new TunnelClient(config);
            client.start();
            this.tunnelClient = client;
            this.sessionConsole = SessionConsole.start(config.getServerAddr(), config.getServerPort());
        } catch (RuntimeException ex) {
            running.set(false);
            stopQuietly();
            throw ex;
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        stopQuietly();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return LIFECYCLE_PHASE;
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getAuth().getToken())) {
            throw new IllegalStateException("orbien.client.auth.token 不能为空");
        }
        if (!StringUtils.hasText(properties.getServerAddr())) {
            throw new IllegalStateException("orbien.client.server-addr 不能为空");
        }
        if (properties.getServerPort() == null || properties.getServerPort() <= 0) {
            throw new IllegalStateException("orbien.client.server-port 必须为正整数");
        }
        if (properties.getProxy().getProtocol() == null) {
            throw new IllegalStateException("orbien.client.proxy.protocol 不能为空");
        }
    }

    private void stopQuietly() {
        SessionConsole console = this.sessionConsole;
        this.sessionConsole = null;
        if (console != null) {
            try {
                console.stop();
            } catch (Exception ignored) {
            }
        }

        TunnelClient client = this.tunnelClient;
        this.tunnelClient = null;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception ignored) {
            }
        }
    }
}
