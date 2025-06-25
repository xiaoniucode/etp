package cn.xilio.vine.console;

import cn.xilio.vine.core.command.CommandMessage;
import cn.xilio.vine.core.command.MethodType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.taobao.text.ui.Element.label;

public class ClientResponseHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        CommandMessage<List> message = CommandMessage.fromJson(text, List.class);
        MethodType method = message.getMethod();
        if ("client_list".equalsIgnoreCase(method.name())){

        }

        // 清空当前行并打印响应
        System.out.print("\033[2K\r"); // ANSI清除整行
       // System.out.println(frame.text());
        TableElement logoTable = new TableElement();
        TableElement data = new TableElement();
        logoTable.row(true,
                label("name\t").style(Decoration.bold.fg(Color.red)),
                label("type\t").style(Decoration.bold.fg(Color.yellow)),
                label("localIP\t").style(Decoration.bold.fg(Color.blue)),
                label("localPort\t").style(Decoration.bold.fg(Color.magenta)),
                label("remotePort\t").style(Decoration.bold.fg(Color.green))
        );
        RowElement rowElement = new RowElement(false);

        rowElement.add("mysql\t","tcp\t","127.0.0.1\t","3306\t","3307\t");


        logoTable.add(rowElement);
        RowElement rowElement2 = new RowElement(false);
        rowElement2.add("redis\t","tcp\t","localhost\t","6379\t","6380\t");
        logoTable.add(rowElement2);

        String render = RenderUtil.render(logoTable );

        System.out.println(render);

        System.out.print("admin $ ");
        System.out.flush(); // 强制刷新缓冲区


    }
}
