package cn.xilio.vine.common;

import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.io.InputStream;

import static com.taobao.text.ui.Element.label;

/**
 * vine banner
 */
public class VineBanner {
    private static final String LOGO_PATH = "logo.txt";

    public static void welcome() {
        System.out.println(logo());
    }
    public static void printLogo() {
        System.out.println(logo());
    }

    /**
     * 打印logo
     *
     * @return 格式化后的logo
     */
    private static String logo() {
        try {
            InputStream in = VineBanner.class.getClassLoader().getResourceAsStream(LOGO_PATH);
            String logoText = IOUtils.toString(in);
            StringBuilder sb = new StringBuilder();
            String[] LOGOS = new String[6];
            int i = 0, j = 0;
            for (String line : logoText.split("\n")) {
                sb.append(line);
                sb.append("\n");
                if (i++ == 6) {
                    LOGOS[j++] = sb.toString();
                    i = 0;
                    sb.setLength(0);
                }
            }

            TableElement logoTable = new TableElement();
            logoTable.row(label(LOGOS[0]).style(Decoration.bold.fg(Color.red)),
                    label(LOGOS[1]).style(Decoration.bold.fg(Color.yellow)),
                    label(LOGOS[2]).style(Decoration.bold.fg(Color.cyan)),
                    label(LOGOS[3]).style(Decoration.bold.fg(Color.magenta)));
            return RenderUtil.render(logoTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
