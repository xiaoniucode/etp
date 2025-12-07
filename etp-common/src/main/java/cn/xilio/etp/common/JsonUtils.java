package com.xiaoniucode.etp.common;

import org.json.JSONObject;
import java.util.Map;

/**
 * @author liuxin
 */
public class JsonUtils {
    public static JSONObject toJsonObject(String jsonString) {
        return new JSONObject(jsonString);
    }
    public static JSONObject toJsonObject(Map<String, Object> map) {
        return new JSONObject(map);
    }
}
