package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.ProxyStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 代理状态转换器
 */
@Converter
public class ProxyStatusConverter implements AttributeConverter<ProxyStatus, Integer> {
    /**
     * 将代理状态转换为数据库列值
     * 
     * @param proxyStatus 代理状态
     * @return 数据库列值
     */
    @Override
    public Integer convertToDatabaseColumn(ProxyStatus proxyStatus) {
        return proxyStatus.getCode();
    }

    /**
     * 将数据库列值转换为代理状态
     * 
     * @param status 数据库列值
     * @return 代理状态
     */
    @Override
    public ProxyStatus convertToEntityAttribute(Integer status) {
        return ProxyStatus.fromStatus(status);
    }
}