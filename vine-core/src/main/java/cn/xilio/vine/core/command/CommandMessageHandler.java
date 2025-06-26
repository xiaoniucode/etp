package cn.xilio.vine.core.command;

import com.google.gson.JsonElement;

public interface CommandMessageHandler {
    String getMethod();
    void handle(JsonElement data) ;
}
