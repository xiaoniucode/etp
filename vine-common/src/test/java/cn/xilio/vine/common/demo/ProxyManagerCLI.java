package cn.xilio.vine.common.demo;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "proxy", description = "内网穿透代理管理工具", subcommands = {
        ProxyManagerCLI.ListCommand.class,
        ProxyManagerCLI.AddCommand.class,
        ProxyManagerCLI.UpdateCommand.class,
        ProxyManagerCLI.DeleteCommand.class
})
public class ProxyManagerCLI implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ProxyManagerCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("欢迎使用内网穿透代理管理工具！");
        CommandLine.usage(this, System.out);
        return 0;
    }

    @Command(name = "list", description = "列出所有代理配置")
    static class ListCommand implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            // 调用 REST API 查询代理配置
            System.out.println("查询代理配置...");
            // 示例：调用 REST API
            //String response = HttpClient.get("http://127.0.0.1:8080/api/proxies");
            System.out.println("response");
            return 0;
        }
    }

    @Command(name = "add", description = "新增代理配置")
    static class AddCommand implements Callable<Integer> {

        @Option(names = {"-n", "--name"}, description = "代理名称", required = true)
        String name;

        @Option(names = {"-l", "--local"}, description = "本地地址", required = true)
        String localAddr;

        @Option(names = {"-r", "--remote"}, description = "远程端口", required = true)
        int remotePort;

        @Override
        public Integer call() throws Exception {
            // 调用 REST API 添加代理配置
            System.out.printf("新增代理：name=%s, localAddr=%s, remotePort=%d%n", name, localAddr, remotePort);
            String payload = String.format("{\"name\":\"%s\", \"localAddr\":\"%s\", \"remotePort\":%d}", name, localAddr, remotePort);
           // String response = HttpClient.post("http://127.0.0.1:8080/api/proxies", payload);
            System.out.println("响应: " + "response");
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
            // 调用 REST API 更新代理配置
            System.out.printf("更新代理：name=%s, localAddr=%s, remotePort=%d%n", name, localAddr, remotePort);
            String payload = String.format("{\"name\":\"%s\", \"localAddr\":\"%s\", \"remotePort\":%d}", name, localAddr, remotePort);
         //   String response = HttpClient.put("http://127.0.0.1:8080/api/proxies/" + name, payload);
            System.out.println("响应: " + "response");
            return 0;
        }
    }

    @Command(name = "delete", description = "删除代理配置")
    static class DeleteCommand implements Callable<Integer> {

        @Parameters(paramLabel = "<name>", description = "代理名称")
        String name;

        @Override
        public Integer call() throws Exception {
            // 调用 REST API 删除代理配置
            System.out.println("删除代理：" + name);
           // String response = HttpClient.delete("http://127.0.0.1:8080/api/proxies/" + name);
            System.out.println("响应: " + "response");
            return 0;
        }
    }
}
