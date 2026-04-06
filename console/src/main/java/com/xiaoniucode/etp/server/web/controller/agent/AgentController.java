package com.xiaoniucode.etp.server.web.controller.agent;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.agent.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.agent.response.AgentDTO;
import com.xiaoniucode.etp.server.web.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 获取所有客户端
     */
    @GetMapping
    public Ajax list(@RequestParam(required = false) String keyword,
                     @RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "10") int size) {
        List<AgentDTO> clients = agentService.findAll(keyword,page,size);
        return Ajax.success(clients);
    }

    /**
     * 根据 ID 获取客户端
     */
    @GetMapping("/{id}")
    public Ajax getById(@PathVariable String id) {
        AgentDTO client = agentService.findById(id);
        return Ajax.success(client);
    }

    /**
     * 删除单个客户端
     */
    @DeleteMapping("/{id}")
    public Ajax delete(@PathVariable String id) {
        agentService.delete(id);
        return Ajax.success();
    }

    /**
     * 批量删除客户端
     */
    @DeleteMapping
    public Ajax deleteBatch(@RequestBody BatchDeleteClientRequest request) {
        agentService.deleteBatch(request);
        return Ajax.success();
    }

    /**
     * 剔除在线客户端
     */
    @PutMapping("/{id}/kickout")
    public Ajax kickout(@PathVariable String id) {
        agentService.kickout(id);
        return Ajax.success();
    }

}
