/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.controller;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlDetailDTO;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleAddParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleUpdateParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlUpdateParam;
import com.xiaoniucode.etp.server.web.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access-control")
public class AccessControlController {
    @Autowired
    private AccessControlService accessControlService;

    @GetMapping("/{proxyId}")
    public Ajax get(@PathVariable String proxyId) {
        AccessControlDetailDTO byProxyId = accessControlService.getByProxyId(proxyId);
        return Ajax.success(byProxyId);
    }

    @PutMapping
    public Ajax update(@RequestBody @Validated AccessControlUpdateParam param) {
        accessControlService.updateAccessControl(param);
        return Ajax.success();
    }

    @PostMapping("rule")
    public Ajax addRule(@RequestBody @Validated AccessControlRuleAddParam param) {
        accessControlService.addRule(param);
        return Ajax.success();
    }

    @PutMapping("rule")
    public Ajax updateRule(@RequestBody @Validated AccessControlRuleUpdateParam param) {
        accessControlService.updateRule(param);
        return Ajax.success();
    }

    @DeleteMapping("rule/{id}")
    public Ajax deleteRule(@PathVariable Long id) {
        accessControlService.deleteRuleById(id);
        return Ajax.success();
    }
}
