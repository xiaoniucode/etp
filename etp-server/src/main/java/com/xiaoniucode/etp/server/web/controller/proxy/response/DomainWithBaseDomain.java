package com.xiaoniucode.etp.server.web.controller.proxy.response;

import java.io.Serializable;

public record DomainWithBaseDomain(
        String domain,
        String baseDomain
) implements Serializable {
}
