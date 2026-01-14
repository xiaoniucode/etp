package com.xiaoniucode.etp.server;

import ch.qos.logback.classic.Level;
import com.xiaoniucode.etp.common.ConfigUtils;
import com.xiaoniucode.etp.common.Constants;
import com.xiaoniucode.etp.common.LogConfig;
import com.xiaoniucode.etp.common.LogbackConfigurator;
import com.xiaoniucode.etp.common.PortChecker;
import com.xiaoniucode.etp.core.event.GlobalEventBus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.event.DatabaseInitEvent;
import com.xiaoniucode.etp.server.event.TunnelBindEvent;
import com.xiaoniucode.etp.server.listener.DatabaseInitListener;
import com.xiaoniucode.etp.server.listener.StaticConfigInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务启动入口
 *
 * @author liuxin
 */
public class TunnelServerStartup {
    private static TunnelServer tunnelServer;
    private static final Logger logger = LoggerFactory.getLogger(TunnelServerStartup.class);

    static {
        System.setProperty("io.netty.leakDetection.level", "DISABLED");
    }

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, Constants.SERVER_CONFIG_NAME);
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
        GlobalEventBus.get().subscribe(TunnelBindEvent.class, new DatabaseInitListener());
        GlobalEventBus.get().subscribe(DatabaseInitEvent.class, new StaticConfigInitListener());

        tunnelServer = ServerFactory.createTunnelServer();
        registerShutdownHook(tunnelServer);
        tunnelServer.start();
    }

    private static void initLogback() {
        LogConfig log = AppConfig.get().getLogConfig();
        new LogbackConfigurator.Builder()
                .setPath(log.getPath())
                .setLogPattern(log.getLogPattern())
                .setArchivePattern(log.getArchivePattern())
                .setLogLevel(log.getLevel())
                .setLogName(log.getName())
                .setMaxHistory(log.getMaxHistory())
                .setTotalSizeCap(log.getTotalSizeCap())
                .addLogger("io.netty.channel.ChannelHandlerMask", Level.INFO)
                .build()
                .configure();
    }

    private static void registerShutdownHook(TunnelServer tunnelServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelServer != null) {
                tunnelServer.stop();
                PortChecker.killPort(tunnelServer.getPort());
            }
        }));
    }
}
