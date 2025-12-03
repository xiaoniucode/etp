package cn.xilio.etp.server.web.server;

/**
 * @author liuxin
 */
public interface Filter {
    void doFilter(RequestContext context, FilterChain chain) throws Exception;

    /**
     * 执行顺序，数值越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
}

