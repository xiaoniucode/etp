package cn.xilio.vine.console;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "vtc",
        version = "VTC 1.0",
        mixinStandardHelpOptions = true,
        description = "内网穿透交互终端控制工具")
public class TelnetConsole implements Runnable {
    @Option(names = {"-H", "--host"},
            required = true,
            description = "连接的主机地址")
    private String host;

    @Option(names = {"-P", "--port"},
            required = true,
            description = "连接的端口号")
    private int port;

    @Option(names = {"-u", "--user"},
            required = true,
            description = "登录用户名")
    private String username;

    @Option(names = {"-p", "--password"},
            required = true,
            description = "登录密码",
            interactive = true,
            arity = "0..1")// 0表示可交互输入，1表示可直接传参)
    private String password;

    @Override
    public void run() {
        ConsoleClient consoleClient = new ConsoleClient();
        consoleClient.setRemoteHost(host);
        consoleClient.setRemotePort(port);
        consoleClient.setUsername(username);
        consoleClient.setPassword(password);
        consoleClient.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consoleClient.stop();
            System.out.println("终端已关闭");
        }));
    }

    public static void main(String[] args) {
        new CommandLine(new TelnetConsole()).execute(args);
    }
}
