package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClientDao extends BaseDao {
    public int insert(JSONObject client) {
        return jdbc.insert("INSERT INTO clients (name, secretKey) VALUES (:name, :secretKey)")
                .bind("name", client.getString("name"))
                .bind("secretKey", client.getString("secretKey"))
                .execute();
    }

    public JSONObject getById(long id) {
        return jdbc.query("SELECT * FROM clients WHERE id = :id")
                .bind("id", id)
                .one();
    }

    public JSONObject getByName(String name) {
        return jdbc.query("SELECT * FROM clients WHERE name = :name")
                .bind("name", name)
                .one();
    }

    public JSONObject getBySecretKey(String secretKey) {
        return jdbc.query("SELECT * FROM clients WHERE secretKey = :secretKey")
                .bind("secretKey", secretKey)
                .one();
    }

    public JSONArray list() {
        return jdbc.query("SELECT * FROM clients ORDER BY createdAt DESC")
                .list();
    }

    public boolean update(long id, String name) {
        int rows = jdbc.update("UPDATE clients SET name = :name, updatedAt = datetime('now') WHERE id = :id")
                .bind("name", name)
                .bind("id", id)
                .execute();
        return rows > 0;
    }

    public boolean deleteById(long id) {
        int rows = jdbc.update("DELETE FROM clients WHERE id = :id")
                .bind("id", id)
                .execute();
        return rows > 0;
    }

    public int count() {
        return jdbc.query("SELECT count(id) as count FROM clients ")
                .one().getInt("count");
    }
}
