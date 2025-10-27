package cn.xilio.etp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 文件工具类
 * @author liuxin
 */
public class PortFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortFileUtil.class);

    private static final String PORT_FILE_NAME = "./ports.txt";

    /**
     * 写入端口信息到 ports.txt 文件
     *
     * @param bindPorts 已绑定端口的列表，每项为 StringBuilder 类型
     */
    public static void writePortsToFile(List<StringBuilder> bindPorts) {
        if (bindPorts == null || bindPorts.isEmpty()) {
            return;
        }
        File file = new File(PORT_FILE_NAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("客户端名称|服务名称|协议|内网端口|外网端口\n");
            writer.write("----------------------------------------------\n");
            for (StringBuilder item : bindPorts) {
                writer.write(item.toString());
                writer.newLine();
            }
            writer.flush();
            LOGGER.info("端口信息已写入文件：{}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("写入端口文件失败", e);
        }
    }
}
