package cn.xilio.vine.console;

import cn.xilio.vine.console.command.ClientCommand;
import cn.xilio.vine.console.command.ProxyCommand;
import org.springframework.util.ObjectUtils;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class CommandLineFactory {
    private static final Map<CommandType, CommandLine> commandLines = new HashMap<>();

    static {
        commandLines.put(CommandType.CLIENT, new CommandLine(new ClientCommand()));
        commandLines.put(CommandType.PROXY, new CommandLine(new ProxyCommand()));
    }

    public static CommandLine getCommand(CommandType type) {
        CommandLine commandLine = commandLines.get(type);
        if (ObjectUtils.isEmpty(commandLine)) {
            throw new RuntimeException("not found commandline for type:" + type);
        }
        return commandLine;
    }
}
