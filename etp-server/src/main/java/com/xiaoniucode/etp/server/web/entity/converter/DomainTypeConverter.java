package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.DomainType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DomainTypeConverter implements AttributeConverter<DomainType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DomainType domainType) {
        if (domainType==null){
            return null;
        }
        return domainType.getCode();
    }

    @Override
    public DomainType convertToEntityAttribute(Integer status) {
        if (status==null){
            return null;
        }
        return DomainType.fromType(status);
    }
}