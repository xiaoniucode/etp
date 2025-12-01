package cn.xilio.etp.server.store;

public class LogConfig {
    private String path;
    private String level;
    private String pattern;

    public LogConfig() {
    }

    public LogConfig(String path, String level, String pattern) {
        this.path = path;
        this.level = level;
        this.pattern = pattern;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
