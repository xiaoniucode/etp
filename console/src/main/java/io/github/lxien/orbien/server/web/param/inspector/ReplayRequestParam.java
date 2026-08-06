package io.github.lxien.orbien.server.web.param.inspector;

import lombok.Data;

@Data
public class ReplayRequestParam {
    private Boolean captureToBuffer;
    private Integer timeoutSeconds;
    private ReplayOverridesParam overrides;
}
