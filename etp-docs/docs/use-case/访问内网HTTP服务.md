---
sidebar_position: 2
---

由于`HTTP` 协议基于`TCP`实现，因此天然支持代理，目前尚未未针对`HTTP`协议实现代理功能。

```json
[[clients.proxies]]
name = "web" 
type = "tcp" #指定为tcp
localPort = 8080
remotePort = 8081
```
