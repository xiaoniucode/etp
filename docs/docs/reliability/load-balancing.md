---
sidebar_position: 1
title: 负载均衡
description: 轮询 / 权重 / 随机 / 最少连接
---

# 负载均衡

为代理配置多个后端目标时，按策略分发流量。适用于 HTTP / HTTPS / TCP / UDP 协议

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
custom_domains = ["web.domain.com"]
load_balance_strategy = "roundrobin"
targets = [
    { host = "127.0.0.1", port = 8001, weight = 1, name = "backend-1" },
    { host = "127.0.0.1", port = 8002, weight = 2, name = "backend-2" }
]
```

## 参数说明

| 参数名                   | 类型     | 默认值        | 描述     | 必填 |
|:----------------------|:-------|:-----------|:-------|:---|
| load_balance_strategy | String | roundrobin | 负载均衡策略 | 否  |
| targets               | Array  |            | 内网服务列表 | 是  |

### 负载均衡算法

| 值          | 描述   |
|:-----------|:-----|
| roundrobin | 轮询   |
| weight     | 加权轮询 |
| random     | 随机   |
| leastconn  | 最少连接 |

### 真实内网服务

| 参数名    | 类型      | 默认值       | 描述   | 必填 |
|:-------|:--------|:----------|:-----|:---|
| host   | String  | 127.0.0.1 | 后端地址 | 否  |
| port   | Integer |           | 后端端口 | 是  |
| weight | Integer | 1         | 权重   | 否  |
| name   | String  |           | 目标名称 | 否  |

:::info 说明
也可使用 `local_ip` / `local_port` 配置单个目标；与 `targets` 可同时存在。
:::
