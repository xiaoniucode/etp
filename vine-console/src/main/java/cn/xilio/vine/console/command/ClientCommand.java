package cn.xilio.vine.console.command;

import cn.xilio.vine.console.ChannelHelper;
import cn.xilio.vine.core.command.CommandMessage;
import cn.xilio.vine.core.command.MethodType;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringEncoder;
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

    @Command(name = "list", description = "查看所有客户端列表", mixinStandardHelpOptions = true)
    static class ClientListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                CommandMessage<?> commandMessage = new CommandMessage<>(MethodType.CLIENT_LIST);

                channel.writeAndFlush(new TextWebSocketFrame(commandMessage.toJson()));

            } else {
                System.err.println("\n连接未就绪");
            }
            return 0;
        }
    }

    @Command(name = "del", description = "删除指定客户端", mixinStandardHelpOptions = true)
    static class ClientDeleteCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "客户端密钥")
        private String secretKey;

        @Override
        public Integer call() {
            System.out.println("执行 client delete 命令，secretKey: " + secretKey);
            // 这里添加删除客户端的逻辑
            return 0;
        }
    }

    @Command(name = "update", description = "更新客户端",usageHelpAutoWidth = true, mixinStandardHelpOptions = true)
    static class ClientUpdateCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "客户端密钥")
        private String secretKey;

        @Option(names = "--name", description = "客户端名称",required = false)
        private String name;

        @Override
        public Integer call() {
            System.out.println("执行 client update 命令，secretKey: " + secretKey + ", name: " + name);
            // 这里添加更新客户端的逻辑
            return 0;
        }
    }

    @Command(name = "add", description = "添加客户端", mixinStandardHelpOptions = true)
    static class ClientAddCommand implements Callable<Integer> {
        @Option(names = {"-n","--name"}, description = "客户端名称", required = false)
        private String name;

        @Override
        public Integer call() {
            System.out.println("执行 client add 命令，name: " + name );
            // 这里添加创建客户端的逻辑
            return 0;
        }
    }
}
