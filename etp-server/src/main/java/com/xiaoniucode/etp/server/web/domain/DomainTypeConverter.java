package com.xiaoniucode.etp.server.web.domain;

import com.xiaoniucode.etp.core.enums.DomainType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DomainTypeConverter implements AttributeConverter<DomainType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DomainType domainType) {
        return domainType.getType();
    }

    @Override
    public DomainType convertToEntityAttribute(Integer status) {
        return DomainType.fromType(status);
    }
}