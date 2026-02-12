package com.xiaoniucode.etp.server.web.controller.proxy.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
public class BatchDeleteRequest implements Serializable {
    private Set<String>ids;
}
