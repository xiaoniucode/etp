---
sidebar_position: 5
title: BasicAuth
description: HTTP / HTTPS 访客侧账号认证
---

# BasicAuth

为 HTTP / HTTPS 代理开启访客侧用户名密码认证（仅这两种协议支持）。

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_ip = "127.0.0.1"
local_port = 8001
custom_domains = ["web.domain.com"]

[proxies.basic_auth]
enabled = true
users = [
    { user = "demo", pass = "demo123" }
]
```

## 参数说明

| 参数名     | 类型      | 默认值   | 描述          | 必填 |
|:--------|:--------|:------|:------------|:---|
| enabled | Boolean | false | 是否开启用户名密码认证 | 否  |
| users   | Array   |       | 认证用户列表      | 否  |

### 用户信息

| 参数名  | 类型     | 默认值 | 描述  | 必填 |
|:-----|:-------|:----|:----|:---|
| user | String |     | 用户名 | 是  |
| pass | String |     | 密码  | 是  |

:::info 说明
用户密码会被加密存储
:::
