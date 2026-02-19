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
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * 访问控制转换类
 */
@Mapper
public interface AccessControlConvert {
    AccessControlConvert INSTANCE = Mappers.getMapper(AccessControlConvert.class);
    @Mapping(source = "mode", target = "mode", qualifiedByName = "accessControlModeToInteger")
    AccessControlDTO toDTO(AccessControl accessControl);

    @Mapping(source = "ruleType", target = "ruleType", qualifiedByName = "accessControlModeToInteger")
    AccessControlRuleDTO toRuleDTO(AccessControlRule accessControlRule);

    @Mapping(source = "mode", target = "mode", qualifiedByName = "integerToAccessControlMode")
    AccessControl toEntity(AddAccessControlRequest request);

    @Mapping(source = "ruleType", target = "ruleType", qualifiedByName = "integerToAccessControlMode")
    AccessControlRule toRuleEntity(AddAccessControlRuleRequest request);

    @Named("accessControlModeToInteger")
    default Integer accessControlModeToInteger(AccessControlMode mode) {
        return mode != null ? mode.getCode() : null;
    }

    @Named("integerToAccessControlMode")
    default AccessControlMode integerToAccessControlMode(Integer code) {
        return code != null ? AccessControlMode.fromCode(code) : null;
    }
}
