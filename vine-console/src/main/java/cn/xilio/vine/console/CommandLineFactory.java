package cn.xilio.vine.console;

import cn.xilio.vine.console.command.ClientCommand;
import cn.xilio.vine.console.command.ProxyCommand;
import org.springframework.util.ObjectUtils;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令工厂，用于命令的创建
 */
public class CommandLineFactory {
    private static final Map<CommandType, CommandLine> commandLines = new HashMap<>();

    static {
        commandLines.put(CommandType.CLIENT, new CommandLine(new ClientCommand()));
        commandLines.put(CommandType.PROXY, new CommandLine(new ProxyCommand()));
    }

    /**
     * 根据类型获取命令
     *
     * @param type 命令类型
     * @return 命令
     */

    public static CommandLine getCommand(CommandType type) {
        CommandLine commandLine = commandLines.get(type);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUnmatchedArgumentsAllowed(false);
        if (ObjectUtils.isEmpty(commandLine)) {
            throw new RuntimeException("not found commandline for type:" + type);
        }
        return commandLine;
    }
}
