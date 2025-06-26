package cn.xilio.vine.core.command.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回结果
 *
 * @param <T>
 */
public abstract class ResultView<T> {
    /**
     * 绘制终端内容
     *
     * @param result 待处理数据
     */
    public abstract void draw(T result);

    /**
     * 输出结果
     *
     * @param data 处理后的数据
     */
    protected void write(String data) {
        // 1. 标准化输入：去除行首尾空格，分割有效行
        String[] lines = data.trim().split("\\s*\n\\s*");
        for (int i = 0; i < lines.length; i++) {
            // 2. 输出
            System.out.println(lines[i]);
        }

    }

}
