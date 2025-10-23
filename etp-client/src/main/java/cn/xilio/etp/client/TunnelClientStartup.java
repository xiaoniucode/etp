package cn.xilio.etp.client;

import cn.xilio.etp.common.ConfigUtils;
import cn.xilio.etp.common.ansi.AnsiLog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TunnelClientStartup {
    private static TunnelClient tunnelClient;

    public static void main(String[] args) {
        String configPath = ConfigUtils.getConfigPath(args, "etpc.toml");
        if (configPath == null) {
            return;
        }
        Config.init(configPath);
        Config config = Config.getInstance();

        registerShutdownHook();
        tunnelClient = new TunnelClient();
        tunnelClient.setServerAddr(config.getServerAddr());
        tunnelClient.setServerPort(config.getServerPort());
        tunnelClient.setSecretKey(config.getSecretKey());
        tunnelClient.setSsl(config.isSsl());
        tunnelClient.start();
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (tunnelClient != null) {
                try {
                    tunnelClient.stop();
                } catch (Exception e) {
                    AnsiLog.error("停止隧道客户端时发生错误: " + e.getMessage());
                }
            }
        }));
    }
}
