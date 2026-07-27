package io.github.lxien.orbien.autoconfigure;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.concurrent.atomic.AtomicInteger;

public class OrbienLocalPortLocator {

    private static final String MANAGEMENT_NAMESPACE = "management";

    private final AtomicInteger port = new AtomicInteger(0);
    private final Environment environment;

    public OrbienLocalPortLocator(Environment environment) {
        this.environment = environment;
    }

    public int getPort() {
        return port.get();
    }

    public int resolveRequired() {
        int current = port.get();
        if (current > 0) {
            return current;
        }

        Integer fallback = readFallbackPort();
        if (fallback != null && fallback > 0) {
            port.compareAndSet(0, fallback);
            current = port.get();
            if (current > 0) {
                return current;
            }
        }

        throw new IllegalStateException(
                "无法确定本地 Web 端口。请确认应用启用了 WebServer，"
                        + "且未将 management 端口误当作业务端口。");
    }

    @EventListener
    public void onWebServerInitialized(ApplicationEvent event) {
        if (!event.getClass().getName().endsWith("WebServerInitializedEvent")) {
            return;
        }
        if (isManagementServer(event)) {
            return;
        }

        try {
            Object webServer = event.getClass().getMethod("getWebServer").invoke(event);
            int serverPort = (int) webServer.getClass().getMethod("getPort").invoke(webServer);
            if (serverPort > 0) {
                port.compareAndSet(0, serverPort);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private Integer readFallbackPort() {
        String localServerPort = environment.getProperty("local.server.port");
        if (localServerPort != null && !localServerPort.trim().isEmpty()) {
            return Integer.parseInt(localServerPort.trim());
        }
        return null;
    }

    private static boolean isManagementServer(Object event) {
        try {
            Object context = event.getClass().getMethod("getApplicationContext").invoke(event);
            try {
                Object namespace = context.getClass().getMethod("getServerNamespace").invoke(context);
                return MANAGEMENT_NAMESPACE.equals(namespace);
            } catch (NoSuchMethodException ignored) {
                Object id = context.getClass().getMethod("getId").invoke(context);
                return id != null && id.toString().contains(MANAGEMENT_NAMESPACE);
            }
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
