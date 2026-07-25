---
sidebar_position: 4
title: 多路复用
description: 单连接承载多条流
---

# 多路复用

在一条隧道连接上承载多条逻辑流，减少连接数。全局默认开启，单个代理可覆盖。

## 全局配置

```toml
# orbien.toml
[transport.multiplex]
enabled = true
```

## 代理级配置

代理级别可以覆盖全局`multiplex`配置

```toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
local_port = 3306
remote_port = 3307

[proxies.transport]
multiplex = false
```

## 参数说明

| 参数名       | 类型      | 默认值  | 描述           | 必填 |
|:----------|:--------|:-----|:-------------|:---|
| enabled   | Boolean | true | 全局是否开启多路复用   | 否  |
| multiplex | Boolean | 继承全局 | 代理级覆盖，是否多路复用 | 否  |

:::info 说明
UDP 代理固定使用多路复用。
:::
