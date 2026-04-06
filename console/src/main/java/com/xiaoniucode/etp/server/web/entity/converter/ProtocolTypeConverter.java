package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.ProtocolType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 协议类型转换器
 */
@Converter
public class ProtocolTypeConverter implements AttributeConverter<ProtocolType, Integer> {
    /**
     * 将协议类型转换为数据库列值
     * 
     * @param protocolType 协议类型
     * @return 数据库列值
     */
    @Override
    public Integer convertToDatabaseColumn(ProtocolType protocolType) {
        if (protocolType == null) {
            return null;
        }
        return protocolType.getCode();
    }

    /**
     * 将数据库列值转换为协议类型
     * 
     * @param code 数据库列值
     * @return 协议类型
     */
    @Override
    public ProtocolType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return ProtocolType.getCode(code);
    }
}