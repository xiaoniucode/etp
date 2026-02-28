package com.xiaoniucode.etp.core.domain;

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

    public AccessControlConfig(Boolean enable, AccessControlMode mode, Set<String> allow, Set<String> deny) {
        this.enable = enable;
        this.mode = mode;
        if (allow != null && !allow.isEmpty()) {
            this.allow.addAll(allow);
        }
        if (deny != null && !deny.isEmpty()) {
            this.deny.addAll(deny);
        }
    }

    public boolean hasAllow() {
        return !allow.isEmpty();
    }
    public boolean hasDeny() {
        return !deny.isEmpty();
    }
}