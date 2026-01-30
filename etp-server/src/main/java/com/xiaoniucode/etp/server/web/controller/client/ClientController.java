//package com.xiaoniucode.etp.server.web.controller.client;
//
//import com.xiaoniucode.etp.server.web.common.Ajax;
//import com.xiaoniucode.etp.server.web.controller.client.request.CreateClientRequest;
//import com.xiaoniucode.etp.server.web.controller.client.request.DeleteClientRequest;
//import com.xiaoniucode.etp.server.web.controller.client.request.GetClientRequest;
//import com.xiaoniucode.etp.server.web.controller.client.request.KickoutClientRequest;
//import com.xiaoniucode.etp.server.web.controller.client.request.UpdateClientRequest;
//import com.xiaoniucode.etp.server.web.service.ClientService;
//import jakarta.validation.Valid;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 客户端接口控制器
// *
// * @author liuxin
// */
//@RestController
//@RequestMapping("/clients")
//public class ClientController {
//
//    private final ClientService clientService;
//
//    public ClientController(ClientService clientService) {
//        this.clientService = clientService;
//    }
//
//    @PostMapping
//    public Ajax createClient(@Valid @RequestBody CreateClientRequest param) {
//        clientService.addClient(param);
//        return Ajax.success("ok");
//    }
//
//    @PutMapping
//    public Ajax updateClient(@Valid @RequestBody UpdateClientRequest param) {
//        clientService.updateClient(param);
//        return Ajax.success("ok");
//    }
//
//    @DeleteMapping
//    public Ajax deleteClient(@Valid @RequestBody DeleteClientRequest param) {
//        clientService.deleteClient(param);
//        return Ajax.success("ok");
//    }
//
//    @PostMapping("/kickout")
//    public Ajax kickoutClient(@Valid @RequestBody KickoutClientRequest param) {
//        clientService.kickoutClient(param);
//        return Ajax.success("ok");
//    }
//
//    @GetMapping
//    public Ajax getClients() {
//        return Ajax.success(clientService.clients());
//    }
//
//    @GetMapping("/detail")
//    public Ajax getClient(@Valid GetClientRequest param) {
//        return Ajax.success(clientService.getClient(param));
//    }
//}
