package com.xiaoniucode.etp.server.web.controller.agent.convert;

import com.xiaoniucode.etp.server.web.controller.agent.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.controller.agent.response.AgentDTO;
import com.xiaoniucode.etp.server.web.entity.Agent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 客户端转换器
 */
@Mapper
public interface ClientConvert {

    ClientConvert INSTANCE = Mappers.getMapper(ClientConvert.class);

    /**
     * 将Agent转换为AgentDTO
     *
     * @param agent 客户端实体
     * @return 客户端DTO
     */
    @Mapping(target = "agentType", expression = "java(agent.getAgentType().getCode())")
    @Mappings({@Mapping(target = "isOnline", ignore = true), @Mapping(target = "token", ignore = true)})
    AgentDTO toDTO(Agent agent);

    /**
     * 将Agent列表转换为AgentDTO列表
     *
     * @param agents 客户端实体列表
     * @return 客户端DTO列表
     */
    List<AgentDTO> toDTOList(List<Agent> agents);

    /**
     * 将ClientSaveRequest转换为Agent
     *
     * @param request 客户端保存请求
     * @return 客户端实体
     */
    @Mappings({
            @Mapping(target = "id", source = "clientId"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    Agent toEntity(ClientSaveRequest request);
}

