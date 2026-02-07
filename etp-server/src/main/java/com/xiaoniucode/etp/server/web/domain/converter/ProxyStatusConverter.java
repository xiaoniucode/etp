package com.xiaoniucode.etp.server.web.domain.converter;

import com.xiaoniucode.etp.core.enums.ProxyStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProxyStatusConverter implements AttributeConverter<ProxyStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProxyStatus proxyStatus) {
        return proxyStatus.getCode();
    }

    @Override
    public ProxyStatus convertToEntityAttribute(Integer status) {
        return ProxyStatus.fromStatus(status);
    }
}