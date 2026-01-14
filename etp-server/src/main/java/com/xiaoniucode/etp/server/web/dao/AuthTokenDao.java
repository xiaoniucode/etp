package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONArray;
import org.json.JSONObject;

public class AuthTokenDao extends BaseDao {

    public int insert(JSONObject token) {
        return jdbc.insert("INSERT INTO auth_tokens (token, uid, username, expiredAt) VALUES (:token, :uid, :username, :expiredAt)")
                .bind("token", token.getString("token"))
                .bind("uid", token.getInt("uid"))
                .bind("username", token.getString("username"))
                .bind("expiredAt", token.getLong("expiredAt"))
                .execute();
    }

    public JSONObject getByToken(String token) {
        return jdbc.query("SELECT uid, username FROM auth_tokens WHERE token = :token AND expiredAt > strftime('%s', 'now')")
                .bind("token", token)
                .one();
    }

    public JSONObject getByTokenIgnoreExpiry(String token) {
        return jdbc.query("SELECT uid, username FROM auth_tokens WHERE token = :token")
                .bind("token", token)
                .one();
    }

    public JSONArray listByClientId(int clientId) {
        return jdbc.query("SELECT * FROM auth_tokens WHERE clientId = :clientId ORDER BY createdAt DESC")
                .bind("clientId", clientId)
                .list();
    }

    public JSONArray listByUserId(int userId) {
        return jdbc.query("SELECT * FROM auth_tokens WHERE userId = :userId ORDER BY createdAt DESC")
                .bind("userId", userId)
                .list();
    }

    public boolean deleteByToken(String token) {
        int rows = jdbc.update("DELETE FROM auth_tokens WHERE token = :token")
                .bind("token", token)
                .execute();
        return rows > 0;
    }

    public int deleteExpiredTokens() {
        return jdbc.update("DELETE FROM auth_tokens WHERE expiredAt <= strftime('%s', 'now')")
                .execute();
    }

    public boolean updateExpiration(String token, String newExpiresAt) {
        int rows = jdbc.update("UPDATE auth_tokens SET expiresAt = :expiresAt WHERE token = :token")
                .bind("expiresAt", newExpiresAt)
                .bind("token", token)
                .execute();
        return rows > 0;
    }

    public JSONArray listAllAuthTokens() {
        return jdbc.query("SELECT * FROM auth_tokens ORDER BY createdAt DESC")
                .list();
    }
}