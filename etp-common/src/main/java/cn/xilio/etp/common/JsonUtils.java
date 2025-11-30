package cn.xilio.etp.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().create();

    public static Map<String, Object> toMap(String jsonString) {
        try {
            // 使用 TypeToken 解决泛型擦除问题
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            return gson.fromJson(jsonString, mapType);
        } catch (Exception e) {
            throw new RuntimeException("JSON 转换失败: " + e.getMessage(), e);
        }
    }
    public static JSONObject toJsonObject(String jsonString) {
        return new JSONObject(jsonString);
    }
    public static <T> T toBean(String jsonString, Class<T> beanClass) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return null;
            }
            return gson.fromJson(jsonString, beanClass);
        } catch (Exception e) {
            throw new RuntimeException("JSON 转换 Bean 失败: " + e.getMessage(), e);
        }
    }
}
