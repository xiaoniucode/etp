package cn.xilio.vine.common;

import java.io.Console;


public class TestConsole {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("当前环境不支持Console，请改用其他方式");
            return;
        }
        String username = console.readLine("用户名: ");
        char[] passwordChars = console.readPassword("密码: "); // 密码隐藏显示
        String password = new String(passwordChars);
        System.out.println("输入完成，用户名=" + username);
    }
}
