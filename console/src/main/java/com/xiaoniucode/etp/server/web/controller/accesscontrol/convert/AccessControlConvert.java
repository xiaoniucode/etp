package com.xiaoniucode.etp.server.web.controller.accesscontrol.convert;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlRuleDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.entity.AccessControl;
import com.xiaoniucode.etp.server.web.entity.AccessControlRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 访问控制转换类
 */
@Mapper(uses = AccessControlMode.class)
public interface AccessControlConvert {
    AccessControlConvert INSTANCE = Mappers.getMapper(AccessControlConvert.class);
    
    @Mapping(target = "mode", expression = "java(accessControl.getMode() != null ? accessControl.getMode().getCode() : null)")
    AccessControlDTO toDTO(AccessControl accessControl);

    @Mapping(target = "ruleType", expression = "java(accessControlRule.getRuleType() != null ? accessControlRule.getRuleType().getCode() : null)")
    AccessControlRuleDTO toRuleDTO(AccessControlRule accessControlRule);

    @Mapping(target = "mode", expression = "java(request.getMode() != null ? com.xiaoniucode.etp.core.enums.AccessControlMode.fromCode(request.getMode()) : null)")
    AccessControl toEntity(AddAccessControlRequest request);

    @Mapping(target = "ruleType", expression = "java(request.getRuleType() != null ? com.xiaoniucode.etp.core.enums.AccessControlMode.fromCode(request.getRuleType()) : null)")
    AccessControlRule toRuleEntity(AddAccessControlRuleRequest request);
}
