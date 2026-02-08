package com.xiaoniucode.etp.server.web.domain.converter;

import com.xiaoniucode.etp.core.enums.ClientType;
import com.xiaoniucode.etp.core.enums.DomainType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ClientTypeConverter implements AttributeConverter<ClientType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ClientType clientType) {
        if (clientType==null){
            return null;
        }
        return clientType.getCode();
    }

    @Override
    public ClientType convertToEntityAttribute(Integer status) {
        if (status==null){
            return null;
        }
        return ClientType.fromCode(status);
    }
}