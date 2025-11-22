---
sidebar_position: 1
---

# 访问内网TCP服务
下面介绍如何将局域网的MySQL服务暴露到公网
### 🖥️服务端
编辑`etps.toml`配置文件
```js
bindPort = 9527

[[clients]]
name = "macos" #客户端名字
secretKey = "Your-secret-key" #认证密钥
[[clients.proxies]]
name = "mysql" #服务名字，自定义
type = "tcp" #传输协议
localPort = 3306 #内网端口
remotePort = 3307 #公网端口，不指定会自动分配
```

### 💻客户端

```js
serverAddr = "x.x.x.x" #公网IP
serverPort = 9527 # bindPort端口
secretKey = "Your-secret-key" #认证密钥，和服务端保持一致
```

接下来用`公网IP`和`3307`端口去连接`MySQL`。
