package cn.xilio.etp.common.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "interactive", description = "示例：交互式参数输入")
public class InteractiveExample implements Callable<Integer> {
    @Option(names = {"-n", "--name"}, description = "用户名称")
    String name;

    @Override
    public Integer call() throws Exception {
        if (name == null || name.isEmpty()) {
            System.out.println("请输入您的名称：");
            Scanner scanner = new Scanner(System.in);
            name = scanner.nextLine();
        }
        System.out.println("您好, " + name + "！");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InteractiveExample()).execute(args);
        System.exit(exitCode);
    }
}
