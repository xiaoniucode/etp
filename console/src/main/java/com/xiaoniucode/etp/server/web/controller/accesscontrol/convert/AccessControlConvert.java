package com.xiaoniucode.etp.server.web.controller.accesscontrol.convert;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.entity.AccessControl;
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


}
