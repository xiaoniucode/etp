---
sidebar_position: 2
title: 健康检查
description: 后端探活与摘除
---

# 健康检查

由客户端定期探测真实服务目标上报给服务端，连续失败达到阈值后摘除，恢复后重新接入。

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
custom_domains = ["web.domain.com"]
targets = [
    { host = "127.0.0.1", port = 8001, weight = 1, name = "backend-1" },
    { host = "127.0.0.1", port = 8002, weight = 1, name = "backend-2" }
]

[proxies.health_check]
enabled = true
type = "http"
interval = 10
timeout = 8
max_failed = 3
path = "/health"
```

## 参数说明

| 参数名        | 类型      | 默认值     | 描述                              | 必填 |
|:-----------|:--------|:--------|:--------------------------------|:---|
| enabled    | Boolean | false   | 是否开启健康检查                        | 否  |
| type       | String  |         | 检查类型：`tcp` / `http`             | 是  |
| interval   | Integer | 10      | 检查间隔（秒）                         | 否  |
| timeout    | Integer | 8       | 超时时间（秒）                         | 否  |
| max_failed | Integer | 3       | 连续失败次数达到后摘除                     | 否  |
| path       | String  | /health | HTTP 检查路径，仅 `type = "http"` 时有效 | 否  |

:::info 说明

- `tcp`：建立 TCP 连接成功即视为健康
- `http`：请求 `path`，成功响应即视为健康
  :::
