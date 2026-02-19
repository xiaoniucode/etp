package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.core.domain.ProxyConfig;
import com.xiaoniucode.etp.core.domain.cidr.CIDRMatcher;
import com.xiaoniucode.etp.core.enums.AccessControlMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessControlManager {
    /**
     * proxyId --> CIDRMatcher
     */
    private final Map<String, CIDRMatcher> matcherMap = new ConcurrentHashMap<>();
    @Autowired
    private ProxyManager proxyManager;

    public boolean checkAccess(String proxyId, String visitorIp) {
        if (proxyId == null || visitorIp == null) {
            return true;
        }
        ProxyConfig proxyConfig = proxyManager.getById(proxyId);
        AccessControlConfig accessControl = proxyConfig.getAccessControl();
        if (accessControl == null || !accessControl.isEnable()) {
            matcherMap.remove(proxyId);
            return true;
        }

        AccessControlMode mode = accessControl.getMode();
        Set<String> allow = accessControl.getAllow();
        if (mode.isAllowMode() && allow.isEmpty()) {
            //白名单模式 + 空列表 = 拒绝所有
            return false;
        }
        Set<String> deny = accessControl.getDeny();
        if (mode.isDenyMode() && deny.isEmpty()) {
            //黑名单模式 + 空列表 = 允许所有
            return true;
        }

        CIDRMatcher matcher = matcherMap.computeIfAbsent(proxyId, s -> new CIDRMatcher(mode, allow, deny));
        return matcher.isAllowed(visitorIp);
    }

    public void remove(String proxyId) {
        matcherMap.remove(proxyId);
    }

    public void clearAll() {
        matcherMap.clear();
    }
}
