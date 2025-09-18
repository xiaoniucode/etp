package cn.xilio.etp.core.command.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 命令消息协议
 */
public class CommandMessage implements Serializable {
    private MethodType method;
    private JsonElement data;

    public CommandMessage(MethodType method) {
        this.method = method;
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    // JSON 反序列化
    public static CommandMessage fromJson(String json) {
        return new Gson().fromJson(json, CommandMessage.class);
    }
}
