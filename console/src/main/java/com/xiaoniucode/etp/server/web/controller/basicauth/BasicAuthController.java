//package com.xiaoniucode.etp.server.web.controller.basicauth;
//
//import com.xiaoniucode.etp.server.web.common.Ajax;
//import com.xiaoniucode.etp.server.web.controller.basicauth.request.AddHttpUserRequest;
//import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateBasicAuthRequest;
//import com.xiaoniucode.etp.server.web.controller.basicauth.request.UpdateHttpUserRequest;
//import com.xiaoniucode.etp.server.web.service.BasicAuthService;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
///**
// * BasicAuth 控制器
// */
//@RestController
//@RequestMapping("/api/basic-auth")
//public class BasicAuthController {
//
//    private final BasicAuthService basicAuthService;
//
//    public BasicAuthController(BasicAuthService basicAuthService) {
//        this.basicAuthService = basicAuthService;
//    }
//
//    @GetMapping("/{proxyId}")
//    public Ajax getByProxyId(@PathVariable String proxyId) {
//        return Ajax.success(basicAuthService.getByProxyId(proxyId));
//    }
//
//    @PutMapping
//    public Ajax update(@Validated @RequestBody UpdateBasicAuthRequest request) {
//        basicAuthService.update(request);
//        return Ajax.success();
//    }
//    @GetMapping("/users/{proxyId}")
//    public Ajax getHttpUsersByProxyId(@PathVariable String proxyId) {
//        return Ajax.success(basicAuthService.getHttpUsersByProxyId(proxyId));
//    }
//    @GetMapping("/users/detail/{id}")
//    public Ajax getHttpUser(@PathVariable Integer id) {
//        return Ajax.success(basicAuthService.getHttpUserById(id));
//    }
//
//    @DeleteMapping("/users/{id}")
//    public Ajax deleteHttpUser(@PathVariable Integer id) {
//        basicAuthService.deleteUser(id);
//        return Ajax.success();
//    }
//
//    @PostMapping("/users")
//    public Ajax addHttpUser(@Validated @RequestBody AddHttpUserRequest request) {
//        basicAuthService.addUser(request);
//        return Ajax.success();
//    }
//
//    @PutMapping("/users")
//    public Ajax updateUser(@Validated @RequestBody UpdateHttpUserRequest request) {
//        basicAuthService.updateUser(request);
//        return Ajax.success();
//    }
//}
