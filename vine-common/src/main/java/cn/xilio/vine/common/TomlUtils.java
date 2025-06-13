package cn.xilio.vine.common;


import com.moandjiezana.toml.Toml;

import java.io.File;

public class TomlUtils {
    public static Toml readToml(String filePath) {
        return new Toml().read(new File(filePath));
    }
}
