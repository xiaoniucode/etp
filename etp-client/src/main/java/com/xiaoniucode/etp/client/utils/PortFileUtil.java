package com.xiaoniucode.etp.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 用于将端口映射信息写到本地文件存储
 *
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
            LOGGER.info("代理规则配置信息已保存到文件：{}", PORT_FILE_NAME);
        } catch (IOException e) {
            LOGGER.error("写入代理规则文件失败", e);
        }
    }
}
