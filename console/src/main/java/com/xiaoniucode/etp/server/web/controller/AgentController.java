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
package com.xiaoniucode.etp.server.web.controller;
import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.dto.agent.AgentDTO;
import com.xiaoniucode.etp.server.web.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    @Autowired
    private AgentService agentService;
    
    @GetMapping("list-by-page")
    public Ajax listByPage(@RequestParam(required = false) String keyword,
                     @RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "10") int size) {
        List<AgentDTO> clients = agentService.findAll(keyword,page,size);
        return Ajax.success(clients);
    }
    
    @GetMapping("list")
    public Ajax listAll() {
        List<AgentDTO> clients = agentService.findAll();
        return Ajax.success(clients);
    }
    
    @GetMapping("/{id}")
    public Ajax getById(@PathVariable String id) {
        AgentDTO client = agentService.findById(id);
        return Ajax.success(client);
    }
    
    @PutMapping("/kickout/{id}")
    public Ajax kickout(@PathVariable String id) {
        agentService.kickout(id);
        return Ajax.success();
    }
}
