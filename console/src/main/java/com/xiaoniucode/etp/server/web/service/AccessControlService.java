package com.xiaoniucode.etp.server.web.service;

import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRuleRequest;

import java.util.List;

/**
 * 访问控制服务
 */
public interface AccessControlService {

    AccessControlDTO getByProxyId(String proxyId);

    void update(UpdateAccessControlRequest request);

    void deleteRuleById(Integer ruleId);

    AccessControlDTO add(AddAccessControlRequest request);

    void addRule(AddAccessControlRuleRequest request);

    void addRules(List<AddAccessControlRuleRequest> rules);

    void updateRule(UpdateAccessControlRuleRequest request);
}
