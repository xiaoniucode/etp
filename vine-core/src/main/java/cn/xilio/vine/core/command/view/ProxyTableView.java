package cn.xilio.vine.core.command.view;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ProxyModel;
import cn.xilio.vine.core.command.model.ResultView;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

import static com.taobao.text.ui.Element.label;

public class ProxyTableView extends ResultView<List<ProxyModel>> {
    @Override
    public void draw(List<ProxyModel> result) {
        TableElement table = new TableElement();
        // 表头仅包含模型实际字段
        table.row(true,
                label("Name").style(Decoration.bold.fg(Color.red)),
                label("Type").style(Decoration.bold.fg(Color.yellow)),
                label("Local IP").style(Decoration.bold.fg(Color.green)),
                label("Local Port").style(Decoration.bold.fg(Color.cyan)),
                label("Remote Port").style(Decoration.bold.fg(Color.magenta)),
                label("Secret Key").style(Decoration.bold.fg(Color.white))
        );

        for (ProxyModel model : result) {
            RowElement rowElement = new RowElement(false); // 显式创建行对象
            rowElement.add(
                    model.getName() ,
                    (model.getType() != null ? model.getType().name() : "N/A") ,
                    model.getLocalIP() ,
                    (model.getLocalPort() != null ? model.getLocalPort().toString() : "N/A") ,
                    (model.getRemotePort() != null ? model.getRemotePort().toString() : "N/A") ,
                    model.getSecretKey()
            );
            table.add(rowElement); // 添加行到表格
        }
        write(RenderUtil.render(table,500));
    }
}
