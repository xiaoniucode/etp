package cn.xilio.vine.client;

import cn.xilio.vine.common.ansi.AnsiLog;
import cn.xilio.vine.common.VineBanner;
import org.springframework.util.StringUtils;

import java.io.File;

public class TunnelClientStartup {
    public static void main(String[] args) {
        if (!checkArgs(args)) {
            return;
        }
        Config.init(args[1]);
        TunnelClient tunnelClient = new TunnelClient();
        tunnelClient.setServerAddr(Config.getServerAddr());
        tunnelClient.setServerPort(Config.getServerPort());
        tunnelClient.setSecretKey(Config.getSecretKey());
        tunnelClient.start();
        VineBanner.printLogo();/*打印logo*/
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
        if (!StringUtils.hasText(args[1])) {
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
