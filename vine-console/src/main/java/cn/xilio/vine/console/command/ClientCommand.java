package cn.xilio.vine.console.command;

import cn.xilio.vine.console.ChannelHelper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "client",
        description = "客户端管理命令",
        version = "VTC 1.0",
        mixinStandardHelpOptions = true,
        subcommands = {
                ClientCommand.ClientListCommand.class,
                ClientCommand.ClientDeleteCommand.class,
                ClientCommand.ClientUpdateCommand.class,
                ClientCommand.ClientAddCommand.class
        }
)
public class ClientCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("请输入有效的 client 子命令：list, delete, update, add");
        return 0;
    }

    @Command(name = "list", description = "查看所有客户端列表")
    static class ClientListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                    channel.writeAndFlush(new TextWebSocketFrame("list"))
                            .addListener(future -> {
                                if (!future.isSuccess()) {
                                    System.err.println("\n发送失败: " + future.cause().getMessage());
                                }
                            });
                } else {
                    System.err.println("\n连接未就绪");
                }
            return 0;
        }
    }

    @Command(name = "delete", description = "删除指定客户端", usageHelpAutoWidth = true)
    static class ClientDeleteCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "客户端的 secretKey（必填）", paramLabel = "<secretKey>")
        private String secretKey;

        @Override
        public Integer call() {
            System.out.println("执行 client delete 命令，secretKey: " + secretKey);
            // 这里添加删除客户端的逻辑
            return 0;
        }
    }

    @Command(name = "update", description = "修改客户端信息")
    static class ClientUpdateCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "客户端的 secretKey")
        private String secretKey;

        @Option(names = "--name", description = "客户端名称")
        private String name;

        @Override
        public Integer call() {
            System.out.println("执行 client update 命令，secretKey: " + secretKey + ", name: " + name);
            // 这里添加更新客户端的逻辑
            return 0;
        }
    }

    @Command(name = "add", description = "添加一个客户端")
    static class ClientAddCommand implements Callable<Integer> {
        @Option(names = "--name", description = "客户端名称", required = true)
        private String name;

        @Option(names = "--secretKey", description = "客户端 secretKey", required = true)
        private String secretKey;

        @Override
        public Integer call() {
            System.out.println("执行 client add 命令，name: " + name + ", secretKey: " + secretKey);
            // 这里添加创建客户端的逻辑
            return 0;
        }
    }
}
