package cn.xilio.vine.console;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.protocol.CommandMessage;
import cn.xilio.vine.core.command.protocol.MethodType;
import cn.xilio.vine.core.command.view.ClientTableView;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

import static com.taobao.text.ui.Element.label;

public class ClientResponseHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        // 清空当前行并打印响应
        System.out.print("\033[2K\r"); // ANSI清除整行
        String text = frame.text();
        CommandMessage<List<ClientModel>> message = CommandMessage.fromJson(text, List.class, ClientModel.class);
        MethodType method = message.getMethod();
        List<ClientModel> data = message.getData();
        if ("client_list".equalsIgnoreCase(method.name())){
            ClientTableView tableView = new ClientTableView();
            tableView.draw(data);
        }

        System.out.print("admin $ ");
        System.out.flush(); // 强制刷新缓冲区


    }
}
