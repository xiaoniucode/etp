---
sidebar_position: 3
title: 隧道 TLS
description: 隧道传输层 TLS / mTLS 加密
---

# 隧道 TLS

客户端与服务端之间的传输隧道加密。

## 单向 TLS

服务端出示证书，不校验客户端证书。客户端未配置 `ca_file`
时不校验服务端证书，客户端可以不用配置，会通过[身份认证](../security/access-token.md)
保证来源客户端合法。

### 服务端

```toml
# orbien-server.toml
[transport.tls]
enabled = true
cert_file = "cert/transport/server.crt"
key_file = "cert/transport/server.key"
```

## 双向 TLS（mTLS）

配置 `ca_file` 即开启 mTLS：服务端要求并校验客户端证书，客户端同时用 CA 校验服务端证书。此时客户端必须配置 `cert_file` 与
`key_file`。

### 服务端

```toml
# orbien-server.toml
[transport.tls]
enabled = true
cert_file = "cert/transport/server.crt"
key_file = "cert/transport/server.key"
ca_file = "cert/transport/ca.crt"
```

### 客户端

```toml
# orbien.toml
[transport.tls]
enabled = true
cert_file = "cert/transport/client.crt"
key_file = "cert/transport/client.key"
ca_file = "cert/transport/ca.crt"
```

## 代理配置案例

单个代理可覆盖是否走加密隧道

```toml
# orbien.toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
local_port = 3306
remote_port = 3307

[proxies.transport]
encrypt = true
```

## 参数说明

| 参数名       | 类型      | 默认值  | 描述                 | 必填  |
|:----------|:--------|:-----|:-------------------|:----|
| enabled   | Boolean | true | 是否启用隧道 TLS         | 否   |
| cert_file | String  |      | 证书文件路径             | 见说明 |
| key_file  | String  |      | 私钥文件路径             | 见说明 |
| ca_file   | String  |      | CA 证书路径；配置后开启 mTLS | 否   |
| key_pass  | String  |      | 私钥密码               | 否   |

:::info 说明

- 服务端启用 TLS 时必须配置 `cert_file` 与 `key_file`
- 配置了 `ca_file` 即开启 mTLS，客户端也必须同时配置 `cert_file` 与 `key_file`
- 未配置 `ca_file` 时为单向 TLS，不强制客户端证书
  :::
