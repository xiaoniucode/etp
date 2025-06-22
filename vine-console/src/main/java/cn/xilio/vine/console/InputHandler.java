package cn.xilio.vine.console;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Scanner;

public class InputHandler implements Runnable {
    private final Channel channel;


    public InputHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("admin $ ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                if ("exit".equalsIgnoreCase(input)) {
                    channel.close().sync();
                    break;
                }
                String[] args = input.split("\\s+");
                CommandType commandType = CommandType.getType(args[0]);
                CommandLine commandLine = CommandLineFactory.getCommand(commandType);
                String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
                commandLine.execute(subArgs);


//                if (channel.isActive()) {
//                    channel.writeAndFlush(new TextWebSocketFrame(input))
//                            .addListener(future -> {
//                                if (!future.isSuccess()) {
//                                    System.err.println("\n发送失败: " + future.cause().getMessage());
//                                }
//                            });
//                } else {
//                    System.err.println("\n连接未就绪");
//                }
            }
        } catch (Exception e) {
            System.err.println("\n输入线程异常: " + e.getMessage());
        }
    }
}
