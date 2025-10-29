package cn.xilio.etp.common;


import com.moandjiezana.toml.Toml;

import java.io.File;

/**
 * @author liuxin
 */
public class TomlUtils {
    public static Toml readToml(String filePath) {
        return new Toml().read(new File(filePath));
    }
}
