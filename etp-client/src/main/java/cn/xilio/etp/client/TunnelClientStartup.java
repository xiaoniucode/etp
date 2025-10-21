package cn.xilio.etp.client;

import cn.xilio.etp.common.ansi.AnsiLog;

import java.io.File;
public class TunnelClientStartup {
    private static TunnelClient tunnelClient;
    public static void main(String[] args) {
        if (!checkArgs(args)) {
            return;
        }
        Config.init(args[1]);
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
    private static boolean checkArgs(String[] args) {
        if (args.length < 2) {
            AnsiLog.error("请指定配置文件！");
            return false;
        }
        if (!"-c".equalsIgnoreCase(args[0])) {
            AnsiLog.error("参数有误，请通过-c <path>指定配置文件！");
            return false;
        }
        if (args[1] == null) {
            AnsiLog.error("配置文件路径不能为空！");
            return false;
        }
        if (!args[1].endsWith(".toml")) {
            AnsiLog.error("配置文件格式有误，请使用xxx.toml格式！");
            return false;
        }
        if (!new File(args[1]).exists()) {
            AnsiLog.error("配置文件不存在，请检查路径是否正确！");
            return false;
        }
        return true;
    }
}
