package cn.xilio.vine.console;



import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public enum CommandType {
    CLIENT, PROXY;

    private static final Map<String, CommandType> VALUE_MAP = new HashMap<>();

    static {
        for (CommandType type : CommandType.values()) {
            VALUE_MAP.put(type.name().toLowerCase(), type);
        }
    }

    public static CommandType getType(String arg) {
        return ObjectUtils.isEmpty(arg) ? null : VALUE_MAP.get(arg.toLowerCase());
    }
}
