---
sidebar_position: 1
---

# 访问内网TCP服务

### 🖥️服务端

```js
bindPort = 9527

[[clients]]
name = "macos" #客户端名字
secretKey = "Your secret key" #认证密钥
[[clients.proxies]]
name = "mysql" #内网服务名字
type = "tcp" #传输协议
localPort = 3306 #内网端口
#remotePort = 3307 #公网端口，不填会自动分配
```

### 💻客户端

```js
serverAddr = "x.x.x.x" #公网IP
serverPort = 9527 #bindPort端口
secretKey = "Your secret key" #认证密钥，和服务端保持一致
```
