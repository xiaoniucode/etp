package cn.xilio.vine.console.command;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "proxy",
        version = "VTC 1.0",
        mixinStandardHelpOptions = true,
        description = "代理管理命令",
        subcommands = {
                ProxyCommand.ProxyListCommand.class,
                ProxyCommand.ProxyAddCommand.class,
                ProxyCommand.ProxyDeleteCommand.class,
                ProxyCommand.ProxyUpdateCommand.class
        })
public class ProxyCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        System.out.println("请输入有效的 proxy 子命令：list, add, delete, update");
        return 0;
    }

    @Command(name = "list", description = "查看所有代理列表")
    static class ProxyListCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "secretKey")
        private String secretKey;
        @Override
        public Integer call() {
            System.out.println("执行 proxy list 命令，显示所有代理");
            // 这里添加查询代理列表的逻辑
            return 0;
        }
    }

    @Command(name = "add", description = "添加一个代理")
    static class ProxyAddCommand implements Callable<Integer> {
        @Option(names = "--name", description = "代理名称")
        private String name;

        @Option(names = "--type", description = "代理类型", required = true)
        private String type;

        @Option(names = "--localIP", description = "本地IP")
        private String localIP = "127.0.0.1";

        @Option(names = "--localPort", description = "本地端口", required = true)
        private int localPort;

        @Option(names = "--remotePort", description = "远程端口", required = true)
        private int remotePort;

        @Override
        public Integer call() {
            System.out.printf("执行 proxy add 命令，name: %s, type: %s, localIP: %s, localPort: %d, remotePort: %d%n",
                    name, type, localIP, localPort, remotePort);
            // 这里添加创建代理的逻辑
            return 0;
        }
    }

    @Command(name = "delete", description = "删除指定代理",mixinStandardHelpOptions = true)
    static class ProxyDeleteCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "代理的 remotePort")
        private int remotePort;

        @Override
        public Integer call() {
            System.out.println("执行 proxy delete 命令，remotePort: " + remotePort);
            // 这里添加删除代理的逻辑
            return 0;
        }
    }

    @Command(name = "update", description = "更新代理信息")
    static class ProxyUpdateCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "代理的 remotePort")
        private int remotePort;

        @Option(names = "--name", description = "代理名称")
        private String name;

        @Option(names = "--type", description = "代理类型")
        private String type;

        @Option(names = "--localIP", description = "本地IP")
        private String localIP;

        @Option(names = "--localPort", description = "本地端口")
        private Integer localPort;

        @Option(names = "--remotePort", description = "远程端口")
        private Integer newRemotePort;

        @Override
        public Integer call() {
            System.out.printf("执行 proxy update 命令，remotePort: %d, name: %s, type: %s, localIP: %s, localPort: %s, newRemotePort: %s%n",
                    remotePort, name, type, localIP, localPort, newRemotePort);
            // 这里添加更新代理的逻辑
            return 0;
        }
    }
}
