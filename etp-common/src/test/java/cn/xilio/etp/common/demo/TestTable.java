package cn.xilio.etp.common.demo;

import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import static com.taobao.text.ui.Element.label;

public class TestTable {
    public static void main(String[] args) {
        TableElement logoTable = new TableElement();
        logoTable.row(
                label("name\t").style(Decoration.bold.fg(Color.red)),
                label("type\t").style(Decoration.bold.fg(Color.yellow)),
                label("localIP\t\t").style(Decoration.bold.fg(Color.blue)),
                label("localPort\t").style(Decoration.bold.fg(Color.magenta)),
                label("remotePort\t").style(Decoration.bold.fg(Color.green))
        );
        String render = RenderUtil.render(logoTable);
        System.out.println(render);
    }
}
