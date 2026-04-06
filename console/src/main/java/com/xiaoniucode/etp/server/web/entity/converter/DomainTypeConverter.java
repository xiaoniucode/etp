package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.DomainType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 域名类型转换器
 */
@Converter
public class DomainTypeConverter implements AttributeConverter<DomainType, Integer> {
    /**
     * 将域名类型转换为数据库列值
     * 
     * @param domainType 域名类型
     * @return 数据库列值
     */
    @Override
    public Integer convertToDatabaseColumn(DomainType domainType) {
        if (domainType == null) {
            return null;
        }
        return domainType.getCode();
    }

    /**
     * 将数据库列值转换为域名类型
     * 
     * @param status 数据库列值
     * @return 域名类型
     */
    @Override
    public DomainType convertToEntityAttribute(Integer status) {
        if (status == null) {
            return null;
        }
        return DomainType.fromType(status);
    }
}