package cn.xilio.vine.common;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.List;
  class Config {
    private Database database;
    private Server server;

      public Database getDatabase() {
          return database;
      }

      public void setDatabase(Database database) {
          this.database = database;
      }

      public Server getServer() {
          return server;
      }

      public void setServer(Server server) {
          this.server = server;
      }

      // Getters & Setters
    public static class Database {
        private String url;
        private String username;
        private int timeout;
        // Getters & Setters

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class Server {
        private List<Integer> ports;
        private boolean enabled;
        // Getters & Setters

        public List<Integer> getPorts() {
            return ports;
        }

        public void setPorts(List<Integer> ports) {
            this.ports = ports;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

public class TomlReader {
    public static void main(String[] args) {
        // 1. 加载 TOML 文件
        Toml toml = new Toml().read(new File("/Users/liuxin/Desktop/vine/vine-server/src/main/resources/proxy.toml"));
        List<Toml> proxies = toml.getTables("proxies");
        for (Toml proxy : proxies) {
            System.out.println(proxy.getString("name"));
            System.out.println(proxy.getString("type"));
            System.out.println(proxy.getLong("localPort"));
        }

//        // 2. 直接读取键值（支持嵌套路径）
//        String dbUrl = toml.getString("database.url");
//        int timeout = toml.getLong("database.timeout").intValue();
//        boolean serverEnabled = toml.getBoolean("server.enabled");
//
//
//        // 3. 读取数组
//        List<Long> ports = toml.getList("server.ports");
//
//        System.out.println("Database URL: " + dbUrl); // jdbc:mysql://localhost:3307/mydb
//        System.out.println("Server Ports: " + ports); // [3306, 3307]

//        Config config = new Toml().read(new File("config.toml")).to(Config.class);
//        System.out.println(config.getDatabase().getUrl());
    }
}
