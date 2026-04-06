package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.controller.agent.convert.ClientConvert;
import com.xiaoniucode.etp.server.web.controller.agent.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.agent.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.controller.agent.response.AgentDTO;
import com.xiaoniucode.etp.server.web.entity.Agent;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import com.xiaoniucode.etp.server.web.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentRepository clientRepository;

    @Override
    public List<AgentDTO> findAll(String keyword, int page, int size) {
        List<Agent> agents;
        if (keyword != null && !keyword.isEmpty()) {
            agents = clientRepository.findByKeyword(keyword);
        } else {
            agents = clientRepository.findAll();
        }
        // 简单的内存分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, agents.size());
        if (start >= agents.size()) {
            return List.of();
        }
        agents = agents.subList(start, end);
        List<AgentDTO> dtos = ClientConvert.INSTANCE.toDTOList(agents);
        // 设置默认值
        dtos.forEach(dto -> {
            dto.setIsOnline(false);
            dto.setToken("token_fefefewfwefddsdfrferfefregrggergregregregrr" + dto.getId());
        });
        return dtos;
    }

    @Override
    public AgentDTO findById(String clientId) {
        Agent agent = clientRepository.findById(clientId).orElse(null);
        if (agent == null) {
            return null;
        }
        AgentDTO dto = ClientConvert.INSTANCE.toDTO(agent);
        // 设置默认值
        dto.setIsOnline(false); // TODO: 从内核获取真实在线状态
        dto.setToken("token_" + dto.getId()); // TODO: 从内核获取真实token
        return dto;
    }

    @Override
    public void delete(String clientId) {
        clientRepository.deleteById(clientId);
        // TODO: 通知内核删除客户端
    }

    @Override
    public void deleteBatch(BatchDeleteClientRequest request) {
        List<String> ids = request.getIds();
        if (ids != null && !ids.isEmpty()) {
            clientRepository.deleteAllById(ids);
            // TODO: 通知内核批量删除客户端
        }
    }

    @Override
    public void kickout(String clientId) {
        // TODO: 通知内核剔除在线客户端
    }

    @Override
    public void saveClient(ClientSaveRequest request) {
        Agent agent = ClientConvert.INSTANCE.toEntity(request);
        clientRepository.save(agent);
        // TODO: 通知内核保存客户端
    }
}
