package com.xiaoniucode.etp.common;

import java.util.HashMap;
import java.util.Map;

public class CommandLineArgs {
    private final Map<String, String> args = new HashMap<>();
    private final Map<String, Option> options = new HashMap<>();
    private final Map<String, String> shortToLong = new HashMap<>();

    public CommandLineArgs() {
    }

    public void registerOption(String name, String shortName, String description, boolean required, boolean hasValue) {
        Option option = new Option(name, shortName, description, required, hasValue);
        options.put(name, option);
        if (shortName != null && !shortName.isEmpty()) {
            shortToLong.put(shortName, name);
        }
    }

    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                i = parseLongArg(arg, args, i);
            } else if (arg.startsWith("-")) {
                i = parseShortArg(arg, args, i);
            } else {
                throw new IllegalArgumentException("未知参数: " + arg);
            }
        }

        validateRequired();
    }

    private int parseLongArg(String arg, String[] args, int index) {
        String name = arg.substring(2);
        Option option = options.get(name);
        if (option == null) {
            throw new IllegalArgumentException("未知参数: " + arg);
        }

        if (option.hasValue) {
            if (index + 1 >= args.length) {
                throw new IllegalArgumentException("参数 " + arg + " 需要值");
            }
            this.args.put(name, args[index + 1]);
            return index + 1;
        } else {
            this.args.put(name, "true");
            return index;
        }
    }

    private int parseShortArg(String arg, String[] args, int index) {
        String shortName = arg.substring(1);
        String name = shortToLong.get(shortName);
        if (name == null) {
            throw new IllegalArgumentException("未知参数: " + arg);
        }

        Option option = options.get(name);
        if (option.hasValue) {
            if (index + 1 >= args.length) {
                throw new IllegalArgumentException("参数 " + arg + " 需要值");
            }
            this.args.put(name, args[index + 1]);
            return index + 1;
        } else {
            this.args.put(name, "true");
            return index;
        }
    }

    private void validateRequired() {
        for (Option option : options.values()) {
            if (option.required && !args.containsKey(option.name)) {
                throw new IllegalArgumentException("缺少必需参数: --" + option.name + " 或 -" + option.shortName);
            }
        }
    }

    public boolean has(String name) {
        return args.containsKey(name);
    }

    public String get(String name) {
        return args.get(name);
    }

    public String get(String name, String defaultValue) {
        return args.getOrDefault(name, defaultValue);
    }

    public int getInt(String name, int defaultValue) {
        String value = args.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("参数 " + name + " 的值必须是整数: " + value);
        }
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String value = args.get(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    private static class Option {
        String name;
        String shortName;
        String description;
        boolean required;
        boolean hasValue;

        Option(String name, String shortName, String description, boolean required, boolean hasValue) {
            this.name = name;
            this.shortName = shortName;
            this.description = description;
            this.required = required;
            this.hasValue = hasValue;
        }
    }
}