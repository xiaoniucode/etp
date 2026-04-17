/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.web.listener;

import com.xiaoniucode.etp.server.statemachine.agent.AgentInfo;
import com.xiaoniucode.etp.server.statemachine.agent.AgentManager;
import com.xiaoniucode.etp.server.web.entity.AgentDO;
import com.xiaoniucode.etp.server.web.repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class AgentInitializer implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(AgentInitializer.class);
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private AgentManager agentManager;
    
    @Override
    public void run(ApplicationArguments args) {
        logger.info("开始初始化客户端节点...");
        
        int page = 0;
        int size = 100;
        long totalProcessed = 0;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AgentDO> pageResult;
        
        do {
            pageResult = agentRepository.findAll(pageable);
            logger.info("处理第 {} 页，每页 {} 条，共 {} 条记录", page + 1, size, pageResult.getTotalElements());
            
            for (AgentDO agentDO : pageResult.getContent()) {
                agentManager.save(toDomain(agentDO));
                totalProcessed++;
            }
            
            page++;
            pageable = PageRequest.of(page, size);
        } while (pageResult.hasNext());
        
        logger.info("客户端节点初始化完成，共处理 {} 条记录", totalProcessed);
    }

    private AgentInfo toDomain(AgentDO agentDO) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentId(agentDO.getId());
        agentInfo.setName(agentDO.getName());
        agentInfo.setToken(agentDO.getToken());
        agentInfo.setAgentType(agentDO.getAgentType());
        agentInfo.setOs(agentDO.getOs());
        agentInfo.setArch(agentDO.getArch());
        agentInfo.setVersion(agentDO.getVersion());
        agentInfo.setLastActiveTime(agentDO.getLastActiveTime());
        agentInfo.setCreatedAt(agentDO.getCreatedAt());
        return agentInfo;
    }
}
