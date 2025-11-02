package cn.xilio.etp.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuxin
 */
public abstract class JsonUtils {
    public static Map<String, Object> jsonToMap(String json) {
        return new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
        }.getType());
    }
}
