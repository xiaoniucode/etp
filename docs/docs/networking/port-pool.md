---
sidebar_position: 2
title: 端口池
description: TCP/UDP 端口分配与回收
---

# 端口池

服务端为 TCP / UDP / SOCKS5 等需要 `remote_port` 的代理预留可分配端口。客户端未指定 `remote_port` 时，从对应协议端口池自动分配。

```toml
# orbien-server.toml
[[port_pool.tcp]]
start = 9050
end = 9060

[[port_pool.tcp]]
single = 9080

[[port_pool.udp]]
start = 9050
end = 9060
```

## 参数说明

每条端口池记录只能配置 `single`，或成对配置 `start` / `end`，二者不能同时出现。

| 参数名    | 类型      | 默认值 | 描述              | 必填 |
|:-------|:--------|:----|:----------------|:---|
| single | Integer |     | 单个端口            | 否  |
| start  | Integer |     | 端口区间起始（含）       | 否  |
| end    | Integer |     | 端口区间结束（含）       | 否  |

:::info 说明
- `[[port_pool.tcp]]` 供 TCP / SOCKS5 等 TCP 类代理使用
- `[[port_pool.udp]]` 供 UDP 代理使用
- 区间为闭区间；可配置多条，系统会合并重叠区间
:::
