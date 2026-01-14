package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONObject;

public class SettingDao extends BaseDao {
    public int update(JSONObject save) {
        return jdbc.update("UPDATE settings SET key = :key, value = :value WHERE id = :id")
                .bind("key", save.getString("key"))
                .bind("value", save.getString("value"))
                .bind("id", save.getString("id"))
                .execute();
    }

    public int insert(JSONObject save) {
        return jdbc.insert("INSERT INTO settings (key, value) VALUES (:key, :value)")
                .bind("key", save.getString("key"))
                .bind("value", save.getString("value"))
                .execute();
    }

    public JSONObject getByKey(String key) {
        return jdbc.query("SELECT id, key, value FROM settings WHERE key = :key")
                .bind("key", key)
                .one();
    }
}