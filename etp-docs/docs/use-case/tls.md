---
sidebar_position: 2
---

# TLS加密传输案例

### 🖥️服务端
```js
bindPort = 9527

ssl = true
[keystore]
path = "./cert/server.p12"
keyPass = "3FbV8R8VW49O"
storePass = "3FbV8R8VW49O"

[[clients]]
name = "macos"
secretKey = "Your secret key" 
[[clients.proxies]]
name = "mysql"
type = "tcp"
localPort = 3306
remotePort = 3307
```

### 💻客户端
```js
serverAddr = "x.x.x.x"
serverPort = 9527
secretKey = "Your secret key"

ssl = true
[truststore]
path = "./cert/client.p12"
storePass = "jesjH9JNLKY3"
```
