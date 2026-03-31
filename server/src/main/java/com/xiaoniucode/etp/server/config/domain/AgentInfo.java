package com.xiaoniucode.etp.server.config.domain;

import com.xiaoniucode.etp.core.enums.AgentType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class AgentInfo implements Serializable {
    private String agentId;
    private String name;
    private AgentType agentType;
    private String token;
    private String version;
    private String os;
    private String arch;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveTime;
    private Boolean online;

    /**
     * 判断设备是否过期
     * @param timeout 超时数值
     * @param unit 时间单位
     * @return true=已过期
     */
    public boolean isExpired(long timeout, ChronoUnit unit) {
        if (timeout <= 0) {
            return false;
        }

        LocalDateTime baseTime = lastActiveTime != null ? lastActiveTime : createdAt;
        if (baseTime == null) {
            return false;
        }

        LocalDateTime expireTime = baseTime.plus(timeout, unit);
        return LocalDateTime.now().isAfter(expireTime);
    }
}
