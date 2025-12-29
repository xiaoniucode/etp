---
sidebar_position: 2
---

由于`HTTP` 协议基于`TCP`实现，因此天然支持HTTP代理，目前版本尚未针对`HTTP/HTTPS`协议实现代理功能，未来会实现。

```toml
[[clients.proxies]]
name = "web" 
type = "tcp"
localPort = 8080
remotePort = 8081
```
