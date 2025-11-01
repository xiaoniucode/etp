package cn.xilio.etp.server.web.framework;

/**
 * @author liuxin
 */
public interface RequestHandler {
    void handle(RequestContext context) throws Exception;
}
