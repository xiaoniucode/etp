package com.xiaoniucode.etp.server.web;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置存储管理
 *
 * @author liuxin
 */
public class ConfigStore {
    /**
     * 新增客户端
     */
    public int  addClient(JSONObject client) {
       return SQLiteUtils.insert(
            "INSERT INTO clients (name, secretKey) VALUES (?, ?)",
            client.getString("name"), client.getString("secretKey")
        );
    }

    /**
     * 根据 ID 查询
     */
    public JSONObject getClientById(long id) {
        return SQLiteUtils.get(
            "SELECT * FROM clients WHERE id = ?",
            id
        );
    }

    /**
     * 根据名称查询
     */
    public JSONObject getClientByName(String name) {
        return SQLiteUtils.get(
            "SELECT * FROM clients WHERE name = ?",
            name
        );
    }

    /**
     * 根据名称查询
     */
    public JSONObject getClientBySecretKey(String secretKey) {
        return SQLiteUtils.get(
            "SELECT * FROM clients WHERE secretKey = ?",
            secretKey
        );
    }

    /**
     * 查询所有客户端
     */
    public JSONArray listClients() {
        return SQLiteUtils.list("SELECT * FROM clients ORDER BY createdAt DESC");
    }

    /**
     * 更新名称
     */
    public boolean updateClient(long id, String name) {
        int rows = SQLiteUtils.update(
            "UPDATE clients SET name = ?, updatedAt = datetime('now') WHERE id = ?",
            name, id
        );
        return rows > 0;
    }

    /**
     * 删除客户端
     */
    public boolean deleteClient(long id) {
        return SQLiteUtils.delete("DELETE FROM clients WHERE id = ?", id) > 0;
    }

    /**
     * 添加代理
     */
    public int addProxy(JSONObject data) {
        return SQLiteUtils.insert(
            "INSERT INTO proxies (clientId, name, type, localPort, remotePort, status,autoRegistered) VALUES (?, ?, ?, ?, ?, ?,?)",
            data.optInt("clientId"),
            data.optString("name"),
            data.optString("type"),
            data.optInt("localPort"),
            data.optInt("remotePort"),
            data.optInt("status", 1),
            data.optInt("autoRegistered")
        );
    }

    /**
     * 查询单个
     */
    public JSONObject getProxyById(int id) {
        return SQLiteUtils.get("SELECT * FROM proxies WHERE id = ?", id);
    }

    /**
     * 查询某个客户端的所有代理
     */
    public JSONArray listProxyByClient(long clientId) {
        return SQLiteUtils.list("SELECT * FROM proxies WHERE clientId = ?", clientId);
    }

    /**
     * 查询所有开启的代理
     */
    public JSONArray listActiveProxies() {
        return SQLiteUtils.list("SELECT * FROM proxies WHERE status = 1 ORDER BY clientId, remotePort");
    }

    /**
     * 更新代理（只传要改的字段 + id）
     */
    public boolean updateProxy(JSONObject data) {
        long id = data.optLong("id");
        if (id <= 0) {
            return false;
        }
        // 动态构建 SET 子句（只更新传了的字段）
        StringBuilder set = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (data.has("name")) {
            set.append("name = ?, ");
            params.add(data.getString("name"));
        }
        if (data.has("type")) {
            set.append("type = ?, ");
            params.add(data.getString("type"));
        }
        if (data.has("localPort")) {
            set.append("localPort = ?, ");
            params.add(data.getInt("localPort"));
        }
        if (data.has("remotePort")) {
            set.append("remotePort = ?, ");
            params.add(data.getInt("remotePort"));
        }
        if (data.has("status")) {
            set.append("status = ?, ");
            params.add(data.getInt("status"));
        }
        if (data.has("autoRegistered")) {
            set.append("autoRegistered = ?, ");
            params.add(data.getInt("autoRegistered"));
        }

        if (params.isEmpty()) {
            return false;
        }
        // 去掉末尾逗号空格，加上时间
        set.setLength(set.length() - 2);
        set.append(", updatedAt = datetime('now')");
        params.add(id);
        String sql = "UPDATE proxies SET " + set + " WHERE id = ?";
        return SQLiteUtils.update(sql, params.toArray()) > 0;
    }

    /**
     * 删除代理
     */
    public boolean deleteProxy(long id) {
        return SQLiteUtils.delete("DELETE FROM proxies WHERE id = ?", id) > 0;
    }

    public JSONArray listAllProxies() {
        String sql = """
            SELECT
                p.id,
                p.clientId,
                p.name,
                p.type,
                p.localPort,
                p.remotePort,
                p.status,
                p.createdAt,
                p.updatedAt,
                p.autoRegistered,
                c.name AS clientName,
                c.secretKey
            FROM
                proxies p
            LEFT JOIN clients c ON p.clientId = c.id
            """;
        return SQLiteUtils.list(sql);
    }

    /**
     * 删除客户端所有的代理配置
     *
     * @param clientId 客户端ID
     * @return 删除条数
     */
    public int deleteProxiesByClient(int clientId) {
        return SQLiteUtils.delete("DELETE FROM proxies WHERE clientId = ?", clientId);
    }

    public JSONObject getProxyByName(String name) {
        return SQLiteUtils.get("SELECT * FROM proxies WHERE name = ?", name);
    }

    public JSONObject getUserByUsername(String username) {
        return SQLiteUtils.get("SELECT * FROM users WHERE username = ?", username);
    }

    public void deleteAllUser() {
        SQLiteUtils.delete("DELETE FROM users");
    }

    public void addUser(JSONObject user) {
        SQLiteUtils.insert(
            "INSERT INTO users (username, password) VALUES (?, ?)",
            user.getString("username"), user.getString("password")
        );
    }

    public JSONObject getUserById(Integer userId) {
        return SQLiteUtils.get("SELECT * FROM users WHERE id = ?", userId);
    }

    public void updateUserPassword(int userId, String password) {
        SQLiteUtils.update(
            "UPDATE users SET password =?  WHERE id = ?",
            password, userId);
    }

    public JSONObject getProxy(int clientId, String name) {
        return SQLiteUtils.get("SELECT * FROM proxies WHERE clientId = ? and name=?", clientId, name);
    }
}

