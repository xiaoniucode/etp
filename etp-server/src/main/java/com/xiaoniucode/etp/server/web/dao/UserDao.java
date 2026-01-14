package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONObject;

public class UserDao extends BaseDao {
    public JSONObject getByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = :username")
                .bind("username", username)
                .one();
    }

    public int deleteAll() {
        return jdbc.update("DELETE FROM users")
                .execute();
    }

    public int insert(JSONObject user) {
       return jdbc.insert("INSERT INTO users (username, password) VALUES (:username, :password)")
                .bind("username", user.getString("username"))
                .bind("password", user.getString("password"))
                .execute();
    }

    public JSONObject getById(Integer userId) {
        return jdbc.query("SELECT * FROM users WHERE id = :id")
                .bind("id", userId)
                .one();
    }

    public void updatePassword(int userId, String password) {
        jdbc.update("UPDATE users SET password = :password WHERE id = :id")
                .bind("password", password)
                .bind("id", userId)
                .execute();
    }
}