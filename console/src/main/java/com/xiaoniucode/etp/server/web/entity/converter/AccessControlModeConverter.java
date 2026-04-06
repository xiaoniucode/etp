package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 访问控制模式转换器
 */
@Converter
public class AccessControlModeConverter implements AttributeConverter<AccessControlMode, Integer> {
    /**
     * 将访问控制模式转换为数据库列值
     * 
     * @param accessControlMode 访问控制模式
     * @return 数据库列值
     */
    @Override
    public Integer convertToDatabaseColumn(AccessControlMode accessControlMode) {
        if (accessControlMode == null) {
            return null;
        }
        return accessControlMode.getCode();
    }

    /**
     * 将数据库列值转换为访问控制模式
     * 
     * @param code 数据库列值
     * @return 访问控制模式
     */
    @Override
    public AccessControlMode convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return AccessControlMode.fromCode(code);
    }
}
