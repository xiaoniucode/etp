package cn.xilio.vine.console;

import cn.xilio.vine.common.VineBanner;

public class TelnetConsole {
    public static void main(String[] args) {
        ConsoleClient consoleClient = new ConsoleClient();
        consoleClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭终端...");
            consoleClient.stop();
            System.out.println("终端已关闭");
        }));
    }
}
