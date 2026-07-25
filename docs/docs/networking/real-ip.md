---
sidebar_position: 4
title: 真实来源 IP
description: 请求头注入或 HAProxy PROXY Protocol 获取真实来源 IP
---

# 真实来源 IP

**Orbien** 支持两种方式获取 / 传递真实访客 IP。

## 方式一：请求头注入

适用于 **HTTP / HTTPS** 代理协议。无需额外配置：服务端收到访客请求后，自动将访客 IP 写入 `X-Forwarded-For`，再转发给内网真实服务，
应用直接读取 `X-Forwarded-For`请求头即可。

```java
var realIp = request.getHeader("X-Forwarded-For");
```

## 方式二：HAProxy PROXY Protocol

适用于 Orbien 前置 HAProxy / Nginx / 云负载均衡的场景。LB 通过 PROXY Protocol v1/v2 把真实访客 IP 传给
Orbien，用于访问控制、日志，以及 HTTP(S)的 `X-Forwarded-For` 注入。

适用代理协议：**HTTP / HTTPS / TCP / SOCKS5**

```toml
# orbien-server.toml
[proxy_protocol]
enabled = true
#strict = false
#trusted_proxies = ["10.0.0.1", "192.168.1.0/24"]
```

内网真实服务同样通过下列方式可获取真实IP (适用于HTTP/HTTPS代理协议)

```java
var realIp = request.getHeader("X-Forwarded-For");
```

### 参数说明

| 参数名             | 类型      | 默认值   | 描述                                | 必填 |
|:----------------|:--------|:------|:----------------------------------|:---|
| enabled         | Boolean | false | 是否启用 PROXY Protocol 解析            | 否  |
| strict          | Boolean | false | `true` 时无 PROXY 头则拒绝；`false` 允许直连 | 否  |
| trusted_proxies | Array   |       | 可信 LB IP 或 CIDR；为空表示信任所有对端        | 否  |

:::info 说明

- 仅当对端命中 `trusted_proxies`（或列表为空）时才信任 PROXY 头，否则忽略并回退到 TCP 对端地址
- 前置 LB 需开启 **PROXY Protocol**，并保证 **Orbien** 服务端看到的是 LB 地址
- 与方式一可叠加：PROXY 先还原真实 IP，HTTP 再注入到 `X-Forwarded-For`
  :::
