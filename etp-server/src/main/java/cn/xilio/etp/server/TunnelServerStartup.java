package cn.xilio.etp.server;

import ch.qos.logback.classic.Level;
import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.LogbackConfigurator;
import cn.xilio.etp.common.PortChecker;
import cn.xilio.etp.common.StringUtils;
import cn.xilio.etp.server.config.AppConfig;
import cn.xilio.etp.server.config.LogConfig;
import cn.xilio.etp.server.web.DashboardApi;
import cn.xilio.etp.server.web.server.NettyWebServer;
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
        //1.加载配置文件
        AppConfig config = AppConfig.get().load(configPath);
        //2.初始化日志配置
        initLogback();
        int bindPort = config.getBindPort();
        if (PortChecker.isPortOccupied(bindPort)) {
            logger.error("{}端口已经被占用", bindPort);
            return;
        }
        registerShutdownHook(tunnelServer);
        tunnelServer = ServerFactory.createTunnelServer();
        tunnelServer.onSuccessListener(v -> {
            EtpInitialize.initDataConfig();
            //启动dashboard服务
            if (config.getDashboard().getEnable()) {
                webServer = ServerFactory.createWebServer();
                DashboardApi.initFilters(webServer.getFilters());/*web过滤器*/
                DashboardApi.initRoutes(webServer.getRouter());/*web接口*/
                webServer.start();
            }
            TcpProxyServer.get().start();
        });
        tunnelServer.start();
    }

    private static void initLogback() {
        LogConfig log = AppConfig.get().getLogConfig();
        String level = log.getLevel();
        Level levelEnum = Level.valueOf(level);
        String path = log.getPath();
        String pattern = log.getPattern();
        new LogbackConfigurator.LogbackConfigBuilder()
                .setLogFilePath(StringUtils.hasText(path) ? path : ("logs" + File.separator + "etps.log"))
                .setArchiveFilePattern(StringUtils.hasText(pattern) ? pattern : ("logs" + File.separator + "etps.%d{yyyy-MM-dd}.log"))
                .setLogLevel(levelEnum == null ? Level.INFO : levelEnum)
                .build()
                .configureLogback();
    }

    private static void registerShutdownHook(TunnelServer tunnelServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelServer != null) {
                tunnelServer.stop();
                PortChecker.killPort(tunnelServer.getPort());
                TcpProxyServer.get().stop();
            }
            if (webServer != null) {
                webServer.stop();
            }
        }));
    }
}
