package cn.xilio.vine.common.demo;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.Color.*;

@Command(name = "InteractiveShell", description = "交互式命令行终端（带监控面板）", mixinStandardHelpOptions = true, subcommands = {
        InteractiveShellWithMonitor.ListCommand.class,
        InteractiveShellWithMonitor.AddCommand.class,
        InteractiveShellWithMonitor.DeleteCommand.class,
        InteractiveShellWithMonitor.UpdateCommand.class,
        InteractiveShellWithMonitor.MonitorCommand.class
})
public class InteractiveShellWithMonitor implements Callable<Integer> {

    private static boolean running = true; // 用于控制交互式终端是否继续运行

    public static void main(String[] args) {
        AnsiConsole.systemInstall(); // 初始化 Jansi
        CommandLine commandLine = new CommandLine(new InteractiveShellWithMonitor());
        Scanner scanner = new Scanner(System.in);

        System.out.println(Ansi.ansi().fg(GREEN).a("欢迎使用交互式命令行终端！输入 'help' 查看可用命令，输入 'exit' 退出。").reset());

        while (running) {
            System.out.print(Ansi.ansi().fg(CYAN).a("root$ ").reset()); // 显示提示符
            String input = scanner.nextLine();

            // 检查是否退出
            if ("exit".equalsIgnoreCase(input.trim())) {
                running = false;
                System.out.println(Ansi.ansi().fg(YELLOW).a("退出终端。再见！").reset());
                break;
            }

            // 解析并执行命令
            try {
                commandLine.execute(input.split("\\s+"));
            } catch (Exception e) {
                System.out.println(Ansi.ansi().fg(RED).a("错误: " + e.getMessage()).reset());
            }
        }
        AnsiConsole.systemUninstall(); // 恢复终端
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
            System.out.println(Ansi.ansi().fg(GREEN).a("执行命令: 列出代理配置...").reset());
            // 模拟输出
            System.out.println(Ansi.ansi().fg(CYAN).a("代理1: 本地地址=127.0.0.1:8080, 远程端口=9000").reset());
            System.out.println(Ansi.ansi().fg(CYAN).a("代理2: 本地地址=127.0.0.1:9090, 远程端口=9001").reset());
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
            System.out.printf(String.valueOf(Ansi.ansi().fg(GREEN).a("添加代理成功！名称: %s, 本地地址: %s, 远程端口: %d%n").reset()), name, localAddr, remotePort);
            return 0;
        }
    }

    @Command(name = "delete", description = "删除代理配置")
    static class DeleteCommand implements Callable<Integer> {
        @Parameters(paramLabel = "<name>", description = "代理名称")
        String name;

        @Override
        public Integer call() throws Exception {
            System.out.printf(name, Ansi.ansi().fg(YELLOW).a("删除代理成功！名称: %s%n").reset());
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
            System.out.printf(String.valueOf(Ansi.ansi().fg(BLUE).a("更新代理成功！名称: %s, 新的本地地址: %s, 新的远程端口: %d%n").reset()),
                    name,
                    localAddr != null ? localAddr : "未修改",
                    remotePort != null ? remotePort : "未修改");
            return 0;
        }
    }

    @Command(name = "monitor", description = "实时监控代理流量和状态")
    static class MonitorCommand implements Callable<Integer> {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        @Override
        public Integer call() throws Exception {
            System.out.println(Ansi.ansi().fg(GREEN).a("启动监控面板，按 Ctrl+C 退出...").reset());

            scheduler.scheduleAtFixedRate(() -> {
                Random random = new Random();
                int traffic1 = random.nextInt(1000); // 模拟代理1流量
                int traffic2 = random.nextInt(1000); // 模拟代理2流量

                System.out.println(Ansi.ansi().eraseScreen().fg(GREEN).a("代理监控面板").reset());
                System.out.printf(String.valueOf(Ansi.ansi().fg(CYAN).a("代理1: 流量: %d KB, 状态: %s%n").reset()),
                        traffic1, traffic1 > 500 ? Ansi.ansi().fg(RED).a("警告").reset() : Ansi.ansi().fg(GREEN).a("正常").reset());
                System.out.printf(String.valueOf(Ansi.ansi().fg(CYAN).a("代理2: 流量: %d KB, 状态: %s%n").reset()),
                        traffic2, traffic2 > 500 ? Ansi.ansi().fg(RED).a("警告").reset() : Ansi.ansi().fg(GREEN).a("正常").reset());

                System.out.println(Ansi.ansi().fg(YELLOW).a("实时更新中...").reset());
            }, 0, 2, TimeUnit.SECONDS); // 每2秒更新一次监控面板

            return 0;
        }
    }
}
