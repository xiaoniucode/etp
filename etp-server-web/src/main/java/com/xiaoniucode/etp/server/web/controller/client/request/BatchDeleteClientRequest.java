package com.xiaoniucode.etp.server.web.controller.client.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BatchDeleteClientRequest implements Serializable {
    List<String> ids;
}
