package cn.xilio.vine.core.command.protocol;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

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
    public static <T> CommandMessage<T> fromJson(String json, Type type) {
        return new Gson().fromJson(json, type);
    }
    public static <T> CommandMessage<List<T>> fromJson(String json, Class<List> listType, Class<T> elementType) {
        Type type = TypeToken.getParameterized(CommandMessage.class, TypeToken.getParameterized(List.class, elementType).getType()).getType();
        return new Gson().fromJson(json, type);
    }

}
