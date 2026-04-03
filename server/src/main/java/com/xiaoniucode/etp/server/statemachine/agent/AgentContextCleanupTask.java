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

package com.xiaoniucode.etp.server.statemachine.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class AgentContextCleanupTask {

    @Autowired
    private AgentManager agentManager;

    @Scheduled(fixedDelay = 30000)
    public void cleanupInactiveContexts() {
        LocalDateTime now = LocalDateTime.now();
        for (AgentContext context : agentManager.getAllAgentContext()) {
            //todo 待完善
            if (ChronoUnit.MINUTES.between(context.getLastActiveTime(), now) >= 1) {
                agentManager.removeAgentContext(context.getAgentInfo().getAgentId());
                if (context.getControl() != null) {
                    context.getControl().close();
                }
            }
        }
    }
}