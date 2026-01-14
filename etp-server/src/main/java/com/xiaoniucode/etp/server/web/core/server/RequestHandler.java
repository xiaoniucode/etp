package com.xiaoniucode.etp.server.web.core.server;

/**
 * @author liuxin
 */
public interface RequestHandler {
    void handle(RequestContext context) throws Exception;
}
