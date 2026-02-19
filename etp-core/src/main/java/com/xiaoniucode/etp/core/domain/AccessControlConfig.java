package com.xiaoniucode.etp.core.domain;

import com.xiaoniucode.etp.common.utils.StringUtils;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@NoArgsConstructor
public class AccessControlConfig implements Serializable {
    @Setter
    private boolean enable = false;
    @Setter
    private AccessControlMode mode;
    private final Set<String> allow = new CopyOnWriteArraySet<>();
    private final Set<String> deny = new CopyOnWriteArraySet<>();

    public AccessControlConfig(Boolean enable, String mode, Set<String> allow, Set<String> deny) {
        this.enable = enable;
        if (!StringUtils.hasText(mode)) {
            throw new IllegalArgumentException("访问控制模式未指定");
        }
        this.mode = AccessControlMode.fromValue(mode);
        if (allow != null && !allow.isEmpty()) {
            this.allow.addAll(allow);
        }
        if (deny != null && !deny.isEmpty()) {
            this.deny.addAll(deny);
        }
    }
}