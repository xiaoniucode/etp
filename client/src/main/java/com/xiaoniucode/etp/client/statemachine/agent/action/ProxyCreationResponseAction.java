package com.xiaoniucode.etp.client.statemachine.agent.action;
import com.xiaoniucode.etp.client.statemachine.agent.AgentContext;
import com.xiaoniucode.etp.client.statemachine.agent.AgentEvent;
import com.xiaoniucode.etp.client.statemachine.agent.AgentState;
import com.xiaoniucode.etp.core.message.Message;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ProxyCreationResponseAction extends AgentBaseAction {
    private final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyCreationResponseAction.class);

    // ANSI 颜色
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BOLD = "\u001B[1m";

    @Override
    protected void doExecute(AgentState from, AgentState to, AgentEvent event, AgentContext context) {
        Message.NewProxyResp response = context.getAndRemoveAs("NEW_PROXY_RESP", Message.NewProxyResp.class);
        String remoteAddr = response.getRemoteAddr();
        String proxyName = response.getProxyName();
        System.out.println(
                "\n" +
                        GREEN + "┌─────────────────────────────────────────┐\n" +
                        "  " + BOLD + "🚀 ETP 远程隧道已就绪" + RESET + GREEN + "\n" +
                        "├─────────────────────────────────────────┤\n" +
                        "  " + CYAN + "📝 代理名称:" + RESET + "  " + YELLOW + BOLD + proxyName + RESET + "\n" +
                        "  " + CYAN + "🌐 远程地址:" + RESET + "  " + YELLOW + BOLD + remoteAddr + RESET + "\n" +
                        "└─────────────────────────────────────────┘" + RESET + "\n"
        );
    }
}