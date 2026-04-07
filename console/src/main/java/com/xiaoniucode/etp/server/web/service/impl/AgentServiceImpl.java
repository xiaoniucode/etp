package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.server.web.controller.agent.convert.ClientConvert;
import com.xiaoniucode.etp.server.web.controller.agent.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.agent.request.ClientSaveRequest;
import com.xiaoniucode.etp.server.web.controller.agent.response.AgentDTO;
import com.xiaoniucode.etp.server.web.entity.Agent;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import com.xiaoniucode.etp.server.web.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentRepository clientRepository;

    @Override
    public List<AgentDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        Page<Agent> agentPage;
        if (keyword != null && !keyword.isEmpty()) {
            agentPage = clientRepository.findByKeyword(keyword, pageable);
        } else {
            agentPage = clientRepository.findAll(pageable);
        }
        
        List<Agent> agents = agentPage.getContent();
        List<AgentDTO> dtos = ClientConvert.INSTANCE.toDTOList(agents);
        dtos.forEach(dto -> {
            dto.setIsOnline(false);
            dto.setToken("token_fefefewfwefddsdfrferfefregrggergregregregrr" + dto.getId());
        });
        return dtos;
    }

    @Override
    public AgentDTO findById(String agentId) {
        Agent agent = clientRepository.findById(agentId).orElse(null);
        if (agent == null) {
            return null;
        }
        AgentDTO dto = ClientConvert.INSTANCE.toDTO(agent);
        // 设置默认值
        dto.setIsOnline(false);
        dto.setToken("token_" + dto.getId());
        return dto;
    }

    @Override
    public void delete(String agentId) {
        clientRepository.deleteById(agentId);
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
    public void kickout(String agentId) {
        // TODO: 通知内核剔除在线客户端
    }

    @Override
    public void saveClient(ClientSaveRequest request) {
        Agent agent = ClientConvert.INSTANCE.toEntity(request);
        clientRepository.save(agent);
        // TODO: 通知内核保存客户端
    }
}
