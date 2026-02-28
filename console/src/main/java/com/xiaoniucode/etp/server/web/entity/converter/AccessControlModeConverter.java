package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AccessControlModeConverter implements AttributeConverter<AccessControlMode, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AccessControlMode accessControlMode) {
        if (accessControlMode == null) {
            return null;
        }
        return accessControlMode.getCode();
    }

    @Override
    public AccessControlMode convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return AccessControlMode.fromCode(code);
    }
}
