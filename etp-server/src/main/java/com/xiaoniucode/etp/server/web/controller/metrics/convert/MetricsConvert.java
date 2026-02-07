package com.xiaoniucode.etp.server.web.controller.metrics.convert;

import com.xiaoniucode.etp.server.metrics.domain.Metrics;
import com.xiaoniucode.etp.server.web.controller.metrics.response.MetricsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 指标数据转换器
 */
@Mapper
public interface MetricsConvert {

    MetricsConvert INSTANCE = Mappers.getMapper(MetricsConvert.class);

    /**
     * 将Metrics转换为MetricsDTO
     * @param metrics 指标数据
     * @return 指标数据DTO
     */
    MetricsDTO toDTO(Metrics metrics);

    /**
     * 将Metrics列表转换为MetricsDTO列表
     * @param metricsList 指标数据列表
     * @return 指标数据DTO列表
     */
    List<MetricsDTO> toDTOList(List<Metrics> metricsList);
}
