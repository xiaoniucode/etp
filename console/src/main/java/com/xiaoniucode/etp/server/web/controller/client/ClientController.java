package com.xiaoniucode.etp.server.web.controller.client;

import com.xiaoniucode.etp.server.web.common.Ajax;
import com.xiaoniucode.etp.server.web.controller.client.request.BatchDeleteClientRequest;
import com.xiaoniucode.etp.server.web.controller.client.response.ClientDTO;
import com.xiaoniucode.etp.server.web.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户端接口控制器
 *
 * @author liuxin
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * 获取所有客户端
     */
    @GetMapping
    public Ajax getAll() {
        List<ClientDTO> clients = clientService.findAll();
        return Ajax.success(clients);
    }

    /**
     * 根据 ID 获取客户端
     */
    @GetMapping("/{id}")
    public Ajax getById(@PathVariable String id) {
        ClientDTO client = clientService.findById(id);
        return Ajax.success(client);
    }

    /**
     * 删除单个客户端
     */
    @DeleteMapping("/{id}")
    public Ajax delete(@PathVariable String id) {
        clientService.delete(id);
        return Ajax.success();
    }

    /**
     * 批量删除客户端
     */
    @DeleteMapping
    public Ajax deleteBatch(@RequestBody BatchDeleteClientRequest request) {
        clientService.deleteBatch(request);
        return Ajax.success();
    }

    /**
     * 剔除在线客户端
     */
    @PutMapping("/{id}/kickout")
    public Ajax kickout(@PathVariable String id) {
        clientService.kickout(id);
        return Ajax.success();
    }

}
