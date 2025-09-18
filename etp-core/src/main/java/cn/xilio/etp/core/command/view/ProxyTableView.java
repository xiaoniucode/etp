package cn.xilio.etp.core.command.view;

import cn.xilio.etp.core.command.model.ProxyModel;
import cn.xilio.etp.core.command.model.ResultView;
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
                label("Name "),
                label("Type "),
                label("LocalIP "),
                label("LocalPort "),
                label("RemotePort "),
                label("SecretKey ")
        );

        for (ProxyModel model : result) {
            RowElement rowElement = new RowElement(false); // 显式创建行对象
            rowElement.add(
                    model.getName() + " ",
                    model.getType().name() + " ",
                    model.getLocalIP() + " ",
                    model.getLocalPort().toString() + " ",
                    model.getRemotePort().toString() + " ",
                    model.getSecretKey() + " "
            );
            table.add(rowElement); // 添加行到表格
        }
        write(RenderUtil.render(table));
    }
}
