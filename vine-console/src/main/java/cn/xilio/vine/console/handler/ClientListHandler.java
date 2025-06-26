package cn.xilio.vine.console.handler;

import cn.xilio.vine.core.command.CommandMessageHandler;
import cn.xilio.vine.core.command.model.ClientModel;
import cn.xilio.vine.core.command.view.ClientTableView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ClientListHandler implements CommandMessageHandler {
    private static final Gson GSON = new Gson();

    @Override
    public String getMethod() {
        return "client_list";
    }

    @Override
    public void handle(JsonElement data) {
        Type type = new TypeToken<List<ClientModel>>() {
        }.getType();
        List<ClientModel> clientData = GSON.fromJson(data, type);
        ClientTableView tableView = new ClientTableView();
        tableView.draw(clientData);
    }
}
