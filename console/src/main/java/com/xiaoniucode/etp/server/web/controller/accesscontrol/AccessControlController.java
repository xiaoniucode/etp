package com.xiaoniucode.etp.server.web.controller.accesscontrol;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.service.AccessControlService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 访问控制控制器
 */
@RestController
@RequestMapping("/api/access-controls")
public class AccessControlController {

    private final AccessControlService accessControlService;

    public AccessControlController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @GetMapping("/proxy/{proxyId}")
    public Ajax getByProxyId(@PathVariable String proxyId) {
        AccessControlDTO dto = accessControlService.getByProxyId(proxyId);
        return Ajax.success(dto);
    }

    @PutMapping
    public Ajax update(@Validated @RequestBody UpdateAccessControlRequest request) {
        accessControlService.update(request);
        return Ajax.success();
    }

    @DeleteMapping("/rules/{ruleId}")
    public Ajax deleteRule(@PathVariable Integer ruleId) {
        accessControlService.deleteRuleById(ruleId);
        return Ajax.success("删除成功");
    }

    @PostMapping("rules")
    public Ajax addRule(@Validated @RequestBody AddAccessControlRuleRequest request) {
       accessControlService.addRule(request);
        return Ajax.success();
    }
    @PutMapping("rules")
    public Ajax updateRule(@Validated @RequestBody UpdateAccessControlRuleRequest request) {
        accessControlService.updateRule(request);
        return Ajax.success();
    }
}
