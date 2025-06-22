package cn.xilio.vine.console;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ClientResponseHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {

        // 清空当前行并打印响应
        System.out.print("\033[2K\r"); // ANSI清除整行
        System.out.println(frame.text());
        System.out.print("admin $ ");
        System.out.flush(); // 强制刷新缓冲区


    }
}
