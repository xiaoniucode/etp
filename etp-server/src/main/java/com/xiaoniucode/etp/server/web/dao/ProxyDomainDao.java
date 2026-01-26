package com.xiaoniucode.etp.server.web.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProxyDomainDao extends BaseDao {
    public int insert(int proxyId, String domain) {
        return jdbc.insert("INSERT OR IGNORE INTO proxy_domains (proxyId, domain) VALUES (:proxyId,:domain)")
                .bind("proxyId", proxyId)
                .bind("domain", domain)
                .execute();
    }

    public JSONObject findProxyName(String domain) {
        return jdbc.query("SELECT * FROM proxy_domains WHERE domain = :domain")
                .bind("domain", domain)
                .one();
    }

    public int deleteByProxyId(int proxyId) {
        return jdbc.update("DELETE FROM proxy_domains WHERE proxyId = :proxyId")
                .bind("proxyId", proxyId)
                .execute();
    }

    public JSONArray listByProxyId(int proxyId) {
        return jdbc.query("SELECT domain FROM proxy_domains WHERE proxyId = :proxyId")
                .bind("proxyId", proxyId)
                .list();
    }

    public int deleteByProxyIds(List<Integer> proxyIds) {
        if (proxyIds.isEmpty()) {
            return 0;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < proxyIds.size(); i++) {
            placeholders.append(":proxyId").append(i);
            if (i < proxyIds.size() - 1) {
                placeholders.append(", ");
            }
        }
        String sql = "DELETE FROM proxy_domains WHERE proxyId IN (" + placeholders + ")";
        var updateBuilder = jdbc.update(sql);
        for (int i = 0; i < proxyIds.size(); i++) {
            updateBuilder = updateBuilder.bind("proxyId" + i, proxyIds.get(i));
        }
        return updateBuilder.execute();
    }

    public JSONArray listAllProxyDomains() {
        return jdbc.query("SELECT proxyId, domain FROM proxy_domains").list();
    }
}
