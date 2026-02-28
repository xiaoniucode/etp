package com.xiaoniucode.etp.server.web.controller.metrics.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record MetricsDTO(
    String key,
    int channels,
    long readBytes,
    long writeBytes,
    long readMessages,
    long writeMessages,
    LocalDateTime time
) implements Serializable {
}
