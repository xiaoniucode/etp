package cn.xilio.vine.console.command;

import cn.xilio.vine.console.ChannelHelper;
import cn.xilio.vine.core.command.protocol.CommandMessage;
import cn.xilio.vine.core.command.protocol.MethodType;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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

    @Command(name = "list", description = "查看所有代理列表", mixinStandardHelpOptions = true)
    static class ProxyListCommand implements Callable<Integer> {
        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                CommandMessage message = new CommandMessage(MethodType.PROXY_LIST);
                channel.writeAndFlush(new TextWebSocketFrame(message.toJson()));
            }
            return 1;
        }
    }

    @Command(name = "add", description = "添加一个代理", mixinStandardHelpOptions = true)
    static class ProxyAddCommand implements Callable<Integer> {
        @Option(names = "--name", description = "代理名称(不填以本地端口命名)", required = false)
        private String name;

        @Option(names = "--type", description = "代理类型(TCP)", required = true)
        private String type;

        @Option(names = "--localIP", description = "本地IP(默认127.0.0.1)", required = false,defaultValue = "127.0.0.1")
        private String localIP;

        @Option(names = "--localPort", description = "本地端口", required = true)
        private int localPort;

        @Option(names = "--remotePort", description = "远程端口", required = true)
        private int remotePort;

        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                CommandMessage message = new CommandMessage(MethodType.PROXY_ADD);
                JsonObject json = new JsonObject();
                json.addProperty("name", name);
                json.addProperty("type", type.toUpperCase());
                json.addProperty("localIP", localIP);
                json.addProperty("localPort", localPort);
                json.addProperty("remotePort", remotePort);
                message.setData(json);
                channel.writeAndFlush(new TextWebSocketFrame(message.toJson()));
            }
            return 0;
        }
    }

    @Command(name = "delete", description = "删除指定代理", mixinStandardHelpOptions = true)
    static class ProxyDeleteCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "代理的 remotePort")
        private int remotePort;

        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                CommandMessage message = new CommandMessage(MethodType.PROXY_DELETE);
                JsonObject json = new JsonObject();
                json.addProperty("remotePort", remotePort);
                message.setData(json);
                channel.writeAndFlush(new TextWebSocketFrame(message.toJson()));
            }
            return 1;
        }
    }

    @Command(name = "update", description = "更新代理信息", mixinStandardHelpOptions = true)
    static class ProxyUpdateCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "代理的 remotePort")
        private int remotePort;

        @Option(names = "--name", description = "代理名称(不填以本地端口命名)")
        private String name;

        @Option(names = "--type", description = "代理类型(TCP)")
        private String type;

        @Option(names = "--localIP", description = "本地IP(默认127.0.0.1)",defaultValue = "127.0.0.1")
        private String localIP;

        @Option(names = "--localPort", description = "本地端口")
        private Integer localPort;

        @Option(names = "--remotePort", description = "远程端口")
        private Integer newRemotePort;

        @Override
        public Integer call() {
            Channel channel = ChannelHelper.get();
            if (channel.isActive()) {
                CommandMessage message = new CommandMessage(MethodType.PROXY_UPDATE);
                JsonObject json = new JsonObject();
                json.addProperty("name", name);
                json.addProperty("type", type.toUpperCase());
                json.addProperty("localIP", localIP);
                json.addProperty("localPort", localPort);
                json.addProperty("remotePort", remotePort);
                json.addProperty("newRemotePort", newRemotePort);
                message.setData(json);
                channel.writeAndFlush(new TextWebSocketFrame(message.toJson()));
            }
            return 0;
        }
    }
}
