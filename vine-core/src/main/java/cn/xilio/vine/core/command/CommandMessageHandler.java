package cn.xilio.vine.core.command;

import com.google.gson.JsonElement;

/**
 * 命令终端消息处理器
 */
public interface CommandMessageHandler {
    /**
     * 方法类型
     * @return 方法类型
     */
    String getMethod();

    /**
     * 处理器业务逻辑
     * @param data 数据
     */
    void handle(JsonElement data) ;
}
