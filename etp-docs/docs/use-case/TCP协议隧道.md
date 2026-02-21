---
sidebar_position: 1
---

## 介绍

`etp`支持两种使用方式，支持纯`Toml`静态配置，也可以采用`图形化界面`实现动态配置端口映射规则，下面介绍如何通过`Toml`静态配置将内网的`MySQL`服务暴露到公网。

### 服务端配置

编辑`etps.toml`配置文件，添加如下配置

```toml
#host="0.0.0.0"
#bindPort = 9527

[[clients]]
name = "client"
secretKey = "认证密钥"
    
[[clients.proxies]]
name = "mysql"
type = "tcp"
localPort = 3306
#remotePort = 3307 #不填系统自动分配
#status=1 #是否开启映射
```

### 客户端配置

```toml
serverAddr = "x.x.x.x"
serverPort = 9527
secretKey = "Your-secret-key"
```

启动`./etpc`客户端后，用`serverAddr`地址和`3307`端口去连接`MySQL`。

参数：
- serverAddr: 服务端所在IP地址，默认127.0.0.1
- serverPort: 服务端bindPort端口，默认9527
- secretKey: 认证密钥
