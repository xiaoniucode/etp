package cn.xilio.vine.core.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;

public class CommandMessage<T> implements Serializable {
    private MethodType method;
    private T data;

    public CommandMessage(MethodType method) {
        this.method = method;
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static <T> CommandMessage<T> fromJson(String json, Class<T> dataType) {
        // 构造包含泛型信息的TypeToken
        Type type = TypeToken.getParameterized(CommandMessage.class, dataType).getType();
        // 执行反序列化
        return new Gson().fromJson(json, type);
    }


}
