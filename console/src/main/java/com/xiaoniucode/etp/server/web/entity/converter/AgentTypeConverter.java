package com.xiaoniucode.etp.server.web.entity.converter;

import com.xiaoniucode.etp.core.enums.AgentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 代理类型转换器
 */
@Converter
public class AgentTypeConverter implements AttributeConverter<AgentType, Integer> {
    /**
     * 将代理类型转换为数据库列值
     * 
     * @param clientType 代理类型
     * @return 数据库列值
     */
    @Override
    public Integer convertToDatabaseColumn(AgentType clientType) {
        if (clientType == null) {
            return null;
        }
        return clientType.getCode();
    }

    /**
     * 将数据库列值转换为代理类型
     * 
     * @param status 数据库列值
     * @return 代理类型
     */
    @Override
    public AgentType convertToEntityAttribute(Integer status) {
        if (status == null) {
            return null;
        }
        return AgentType.fromCode(status);
    }
}