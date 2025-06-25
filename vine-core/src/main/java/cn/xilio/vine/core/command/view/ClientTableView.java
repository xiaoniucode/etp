package cn.xilio.vine.core.command.view;

import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.model.ResultView;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

import static com.taobao.text.ui.Element.label;

/**
 * 客户端列表表格渲染
 */
public class ClientTableView extends ResultView<List<ClientModel>> {
    @Override
    public void draw(List<ClientModel> result) {
        TableElement table = new TableElement();
        table.row(true,
                label("name\t").style(Decoration.bold.fg(Color.red)),
                label("type\t").style(Decoration.bold.fg(Color.yellow)),
                label("status\t").style(Decoration.bold.fg(Color.blue))
        );
        for (ClientModel model : result) {
            String status = getStatusLabel(model.getStatus());
            RowElement rowElement = new RowElement(false);
            rowElement.add(model.getName() + "\t", model.getSecretKey() + "\t" + status + "\t");
            table.add(rowElement);
        }
        write(RenderUtil.render(table));
    }

    private String getStatusLabel(int status) {
        return status == 1 ? "在线" : "离线";
    }
}
