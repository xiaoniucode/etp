package cn.xilio.vine.common;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(name = "InteractiveShell", description = "交互式命令行终端", mixinStandardHelpOptions = true, subcommands = {
        InteractiveShell.ListCommand.class,
        InteractiveShell.AddCommand.class,
        InteractiveShell.DeleteCommand.class,
        InteractiveShell.UpdateCommand.class
})
public class InteractiveShell implements Callable<Integer> {

    private static boolean running = true; // 用于控制交互式终端是否继续运行

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new InteractiveShell());
        Scanner scanner = new Scanner(System.in);

        System.out.println("欢迎使用交互式命令行终端！输入 'help' 查看可用命令，输入 'exit' 退出。");

        while (running) {
            System.out.print("> "); // 提示符
            String input = scanner.nextLine();

            // 检查是否退出
            if ("exit".equalsIgnoreCase(input.trim())) {
                running = false;
                System.out.println("退出终端。再见！");
                break;
            }

            // 解析并执行命令
            try {
                commandLine.execute(input.split("\\s+"));
            } catch (Exception e) {
                System.out.println("错误: " + e.getMessage());
            }
        }
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("这是交互式终端。输入 'help' 查看命令列表，输入 'exit' 退出。");
        return 0;
    }

    @Command(name = "list", description = "列出所有代理配置")
    static class ListCommand implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("执行命令: 列出代理配置...");
            // 模拟输出
            System.out.println("代理1: 本地地址=127.0.0.1:8080, 远程端口=9000");
            System.out.println("代理2: 本地地址=127.0.0.1:9090, 远程端口=9001");
            return 0;
        }
    }

    @Command(name = "add", description = "添加新代理配置")
    static class AddCommand implements Callable<Integer> {
        @Option(names = {"-n", "--name"}, description = "代理名称", required = true)
        String name;

        @Option(names = {"-l", "--local"}, description = "本地地址 (如 127.0.0.1:8080)", required = true)
        String localAddr;

        @Option(names = {"-r", "--remote"}, description = "远程端口", required = true)
        int remotePort;

        @Override
        public Integer call() throws Exception {
            System.out.printf("添加代理成功！名称: %s, 本地地址: %s, 远程端口: %d%n", name, localAddr, remotePort);
            return 0;
        }
    }

    @Command(name = "delete", description = "删除代理配置")
    static class DeleteCommand implements Callable<Integer> {
        @Parameters(paramLabel = "<name>", description = "代理名称")
        String name;

        @Override
        public Integer call() throws Exception {
            System.out.printf("删除代理成功！名称: %s%n", name);
            return 0;
        }
    }

    @Command(name = "update", description = "更新代理配置")
    static class UpdateCommand implements Callable<Integer> {
        @Parameters(paramLabel = "<name>", description = "代理名称")
        String name;

        @Option(names = {"-l", "--local"}, description = "新的本地地址")
        String localAddr;

        @Option(names = {"-r", "--remote"}, description = "新的远程端口")
        Integer remotePort;

        @Override
        public Integer call() throws Exception {
            System.out.printf("更新代理成功！名称: %s, 新的本地地址: %s, 新的远程端口: %d%n",
                    name,
                    localAddr != null ? localAddr : "未修改",
                    remotePort != null ? remotePort : "未修改");
            return 0;
        }
    }
}
