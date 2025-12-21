package com.xiaoniucode.etp.server;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.ConfigUtils;
import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.common.LogbackConfigurator;
import com.xiaoniucode.etp.common.PortChecker;
import com.xiaoniucode.etp.common.StringUtils;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.LogConfig;
import com.xiaoniucode.etp.server.proxy.TcpProxyServer;
import com.xiaoniucode.etp.server.web.DashboardApi;
import com.xiaoniucode.etp.server.web.server.NettyWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 代理服务启动类
 *
 * @author liuxin
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;
    private static NettyWebServer webServer;
    private static final Logger logger = LoggerFactory.getLogger(TunnelServerStartup.class);

    static {
        System.setProperty("io.netty.leakDetection.level", "SIMPLE");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, Constants.DEFAULT_SERVER_CONFIG_NAME);
        if (configPath == null) {
            System.err.println("请指定配置文件路径！");
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
                logger.info("Dashboard图形面板启动成功，浏览器访问：{}:{}", webServer.getAddr(), webServer.getPort());
            }
            TcpProxyServer.get().start();
        });
        tunnelServer.start();
    }

    private static void initLogback() {
        LogConfig log = AppConfig.get().getLogConfig();
        String leve = log.getLevel();
        String path = log.getPath();
        String pattern = log.getPattern();
        new LogbackConfigurator.LogbackConfigBuilder()
            .setLogFilePath(StringUtils.hasText(path) ? path : (Constants.LOG_BASE_PATH + Constants.DEFAULT_SERVER_LOG_NAME))
            .setArchiveFilePattern(StringUtils.hasText(pattern) ? pattern : (Constants.LOG_BASE_PATH + Constants.DEFAULT_SERVER_LOG_PATTERN))
            .setLogLevel(!StringUtils.hasText(leve) ? Level.INFO : Level.toLevel(leve, Level.INFO))
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
