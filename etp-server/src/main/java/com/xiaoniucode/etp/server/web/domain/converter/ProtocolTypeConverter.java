package com.xiaoniucode.etp.server.web.domain.converter;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProtocolTypeConverter implements AttributeConverter<ProtocolType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProtocolType protocolType) {
        if (protocolType == null) {
            return null;
        }
        return protocolType.getCode();
    }

    @Override
    public ProtocolType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return ProtocolType.getCode(code);
    }
}