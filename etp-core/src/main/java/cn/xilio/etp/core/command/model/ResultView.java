package cn.xilio.etp.core.command.model;

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
        // 按行分割
        String[] rows = data.split("\n");

        // 获取表头（第一行）并确定列数
        String[] headerCols = rows[0].trim().split("\\s+");
        int numCols = headerCols.length;

        // 存储每列的最大宽度
        int[] maxWidths = new int[numCols];

        // 初始化最大宽度为表头宽度
        for (int j = 0; j < headerCols.length; j++) {
            maxWidths[j] = headerCols[j].length();
        }

        // 存储数据行的列数据（跳过第二行的横线）
        String[][] dataCols = new String[rows.length - 2][numCols];

        // 第一遍：收集数据行并更新每列最大宽度
        for (int i = 2; i < rows.length; i++) { // 从第三行开始（跳过表头和横线）
            String[] cols = rows[i].trim().split("\\s+", numCols);
            dataCols[i - 2] = cols;

            // 更新每列最大宽度
            for (int j = 0; j < cols.length; j++) {
                maxWidths[j] = Math.max(maxWidths[j], cols[j].length());
            }
        }

        // 输出表头
        StringBuilder headerRow = new StringBuilder();
        for (int j = 0; j < headerCols.length; j++) {
            headerRow.append(String.format("%-" + (maxWidths[j] + 2) + "s", headerCols[j]));
        }
        System.out.println(headerRow);

        // 输出重新生成的连续横线分割行
        StringBuilder separatorRow = new StringBuilder();
        for (int width : maxWidths) {
             //使用循环生成横线
            for (int i = 0; i < width + 2; i++) { // 包含2个空格的宽度
                separatorRow.append("-");
            }
        }
        System.out.println(separatorRow);

        // 输出数据行
        for (String[] cols : dataCols) {
            StringBuilder formattedRow = new StringBuilder();
            for (int j = 0; j < cols.length; j++) {
                formattedRow.append(String.format("%-" + (maxWidths[j] + 2) + "s", cols[j]));
            }
            System.out.println(formattedRow);
        }

    }

}
