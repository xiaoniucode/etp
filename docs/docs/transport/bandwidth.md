---
sidebar_position: 6
title: 带宽限流
description: 入站 / 出站 / 总量限速策略
---

# 带宽限流

按代理限制带宽。单位区分大小写，格式如 `10Mbps`、`100Kbps`、`1Gbps`

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_port = 8001
custom_domains = ["web.domain.com"]

[proxies.bandwidth]
limit_total = "10Mbps"
limit_in = "5Mbps"
limit_out = "5Mbps"
```

## 参数说明

| 参数名         | 类型     | 默认值 | 描述     | 必填 |
|:------------|:-------|:----|:-------|:---|
| limit_total | String |     | 总带宽上限  | 否  |
| limit_in    | String |     | 入站带宽上限 | 否  |
| limit_out   | String |     | 出站带宽上限 | 否  |

:::info 说明
单位支持 `bps` / `Kbps` / `Mbps` / `Gbps`，必须为正整数且无空格
:::
