/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.service.impl;
import com.xiaoniucode.etp.server.web.dto.agent.AgentDTO;
import com.xiaoniucode.etp.server.web.entity.AgentDO;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import com.xiaoniucode.etp.server.web.service.AgentService;
import com.xiaoniucode.etp.server.web.service.converter.AgentConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AgentServiceImpl implements AgentService {
    @Autowired
    private AgentRepository agentRepository;
    @Override
    public List<AgentDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AgentDO> agentPage;
        if (keyword != null && !keyword.isEmpty()) {
            agentPage = agentRepository.findByKeyword(keyword, pageable);
        } else {
            agentPage = agentRepository.findAll(pageable);
        }
        List<AgentDO> agents = agentPage.getContent();
        List<AgentDTO> dtos = AgentConvert.INSTANCE.toDTOList(agents);
        dtos.forEach(dto -> {
            dto.setIsOnline(false);
            dto.setToken("token_fefefewfwefddsdfrferfefregrggergregregregrr" + dto.getId());
        });
        return dtos;
    }
    @Override
    public AgentDTO findById(String agentId) {
        AgentDO agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) {
            return null;
        }
        AgentDTO dto = AgentConvert.INSTANCE.toDTO(agent);
        dto.setIsOnline(false);
        dto.setToken("token_" + dto.getId());
        return dto;
    }
    @Override
    public void kickout(String agentId) {
    }
    @Override
    public List<AgentDTO> findAll() {
        List<AgentDO> all = agentRepository.findAll();
        return AgentConvert.INSTANCE.toDTOList(all);
    }
}
