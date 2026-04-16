/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.service.converter;

import com.xiaoniucode.etp.core.enums.AccessControl;
import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlDetailDTO;
import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlRuleDTO;
import com.xiaoniucode.etp.server.web.entity.AccessControlDO;
import com.xiaoniucode.etp.server.web.entity.AccessControlRuleDO;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleAddParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleUpdateParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlUpdateParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring",imports = {AccessControl.class})
public interface AccessControlConvert {

    @Mapping(expression = "java(AccessControl.fromCode(param.getRuleType()))", target = "mode")
    AccessControlRuleDO toRuleDO(AccessControlRuleAddParam param);

    @Mapping(expression = "java(AccessControl.fromCode(param.getMode()))", target = "mode")
    @Mapping(target = "proxyId",ignore = true)
    void updateDO(@MappingTarget AccessControlDO accessControlDO, AccessControlUpdateParam param);

    @Mapping(expression = "java(AccessControl.fromCode(param.getRuleType()))", target = "mode")
    void updateRuleDO(@MappingTarget AccessControlRuleDO accessControlRuleDO, AccessControlRuleUpdateParam param);

    @Mapping(expression = "java(accessControlDO.getMode().getCode())", target = "mode")
    @Mapping(expression = "java(toRuleDTOList(rules))", target = "rules")
    AccessControlDetailDTO toDetailDTO(AccessControlDO accessControlDO, List<AccessControlRuleDO> rules);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "proxyId", target = "proxyId")
    @Mapping(source = "cidr", target = "cidr")
    @Mapping(expression = "java(ruleDO.getMode().getCode())", target = "ruleType")
    AccessControlRuleDTO toRuleDTO(AccessControlRuleDO ruleDO);

    List<AccessControlRuleDTO> toRuleDTOList(List<AccessControlRuleDO> rules);
}

