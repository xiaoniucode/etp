package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.AgentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ClientTypeConverter implements AttributeConverter<AgentType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AgentType clientType) {
        if (clientType==null){
            return null;
        }
        return clientType.getCode();
    }

    @Override
    public AgentType convertToEntityAttribute(Integer status) {
        if (status==null){
            return null;
        }
        return AgentType.fromCode(status);
    }
}