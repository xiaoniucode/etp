package cn.xilio.vine.console.handler;

import cn.xilio.vine.core.command.CommandMessageHandler;
import cn.xilio.vine.core.command.model.ProxyModel;
import cn.xilio.vine.core.command.protocol.MethodType;
import cn.xilio.vine.core.command.view.ProxyTableView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ProxyListHandler implements CommandMessageHandler {
    private static final Gson GSON = new Gson();

    @Override
    public String getMethod() {
        return MethodType.PROXY_LIST.name();
    }

    @Override
    public void handle(JsonElement data) {
        Type type = new TypeToken<List<ProxyModel>>() {}.getType();
        List<ProxyModel> proxyData = GSON.fromJson(data, type);
        ProxyTableView proxyTableView = new ProxyTableView();
        proxyTableView.draw(proxyData);
    }
}
