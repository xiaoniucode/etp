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

package com.xiaoniucode.etp.server.service;

import com.xiaoniucode.etp.core.enums.AgentType;
import com.xiaoniucode.etp.server.service.repository.AgentQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AgentQueryRepositoryRouter {
    @Autowired
    @Qualifier("standaloneAgentQueryRepository")
    private AgentQueryRepository standalone;
    @Autowired
    @Qualifier("embeddedAgentQueryRepository")
    private AgentQueryRepository embedded;

    public AgentQueryRepository route(AgentType agentType) {
        if (agentType.isEmbedded()) {
            return embedded;
        }
        return standalone;
    }
}