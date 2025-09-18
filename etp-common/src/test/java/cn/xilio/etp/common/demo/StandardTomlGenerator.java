package cn.xilio.etp.common.demo;

import com.moandjiezana.toml.TomlWriter;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StandardTomlGenerator {
    public static void main(String[] args) throws Exception {
        // 1. 构建嵌套数据结构
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> database = new HashMap<>();
        database.put("url", "jdbc:mysql://localhost:3307/mydb");
        database.put("username", "admin");
        database.put("timeout", 30);

        Map<String, Object> server = new HashMap<>();
        server.put("ports", Arrays.asList(3306, 3307));
        server.put("enabled", true);

        config.put("database", database);
        config.put("server", server);

        // 2. 使用 toml4j 生成标准格式
        TomlWriter writer = new TomlWriter();
        writer.write(config, new File("config.toml"));
    }
}
