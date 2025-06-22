package cn.xilio.vine.console;

import picocli.CommandLine;
import java.util.Arrays;
import java.util.Scanner;

public class InputHandler implements Runnable {
    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.print("admin $ ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;
                if ("exit".equalsIgnoreCase(input)) {
                    ChannelHelper.get().close().sync();
                    break;
                }
                String[] args = input.split("\\s+");
                CommandType commandType = CommandType.getType(args[0]);
                CommandLine commandLine = CommandLineFactory.getCommand(commandType);
                String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
                commandLine.execute(subArgs);
            }
        } catch (Exception e) {
            System.err.println("\n输入线程异常: " + e.getMessage());
        }
    }
}
