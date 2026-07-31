---
sidebar_position: 6
title: 带宽限流
description: 按代理限制共享总带宽
---

# 带宽限流

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_port = 8001
custom_domains = ["web.domain.com"]
bandwidth = "10Mbps"
```

## 参数说明

| 参数名       | 类型     | 默认值 | 描述    | 必填 |
|:----------|:-------|:----|:------|:---|
| bandwidth | String |     | 总带宽上限 | 否  |

:::info 说明
单位固定为 `Mbps`，必须为正整数且无空格，例如 `10Mbps`。
:::
