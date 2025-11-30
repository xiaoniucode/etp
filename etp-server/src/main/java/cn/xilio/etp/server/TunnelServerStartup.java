package cn.xilio.etp.server;

import ch.qos.logback.classic.Level;
import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.LogbackConfigurator;
import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.server.store.Config;
import cn.xilio.etp.server.web.DashboardApi;
import cn.xilio.etp.server.web.framework.NettyWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author liuxin
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;
    private static NettyWebServer webServer;
    private static final String DEFAULT_CONFIG_NAME = "etps.toml";
    private static final Logger logger = LoggerFactory.getLogger(TunnelServerStartup.class);

    static {
        System.setProperty("io.netty.leakDetection.level", "SIMPLE");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, DEFAULT_CONFIG_NAME);
        if (configPath == null) {
            return;
        }
        //加载配置文件
        Config.init(configPath);
        initLogback();/*初始化日志*/
        Integer bindPort = Config.getInstance().getBindPort();
        if (PortChecker.isPortOccupied(bindPort)) {
            logger.error("{}端口已经被占用", bindPort);
            return;
        }
        registerShutdownHook(tunnelServer);
        tunnelServer = ServerFactory.createTunnelServer();
        tunnelServer.onSuccessListener(v -> {
            //绑定所有代理端口
            TcpProxyServer.getInstance().start();
            //启动dashboard服务
            if (Config.getInstance().getDashboard().getEnable()) {
                webServer = ServerFactory.createWebServer();
                DashboardApi.initFilters(webServer.getFilters());/*web过滤器*/
                DashboardApi.initRoutes(webServer.getRouter());/*web接口*/
                webServer.start();
            }
        });
        tunnelServer.start();
    }

    private static void initLogback() {
        new LogbackConfigurator.LogbackConfigBuilder()
                .setLogFilePath("logs" + File.separator + "etps.log")
                .setArchiveFilePattern("logs" + File.separator + "etps.%d{yyyy-MM-dd}.log")
                .setLogLevel(Level.DEBUG)
                .build()
                .configureLogback();
    }

    private static void registerShutdownHook(TunnelServer tunnelServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelServer != null) {
                tunnelServer.stop();
                PortChecker.killPort(tunnelServer.getPort());
                TcpProxyServer.getInstance().stop();
            }
            if (webServer != null) {
                webServer.stop();
            }
        }));
    }
}
