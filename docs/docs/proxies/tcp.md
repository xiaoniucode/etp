---
sidebar_position: 3
title: TCP
description: 通用 TCP 端口映射
---

# TCP 代理

```toml
# orbien.toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
local_ip = "127.0.0.1"
local_port = 3306
remote_port = 3307
```

## 参数说明

| 参数名         | 类型      | 默认值       | 描述              | 必填 |
|:------------|:--------|:----------|:----------------|:---|
| name        | String  |           | 代理名称，唯一标识一个代理配置 | 是  |
| protocol    | String  |           | 协议类型            | 是  |
| local_ip    | String  | 127.0.0.1 | 本地IP            | 否  |
| local_port  | Integer |           | 本地端口            | 是  |
| remote_port | Integer |           | 远程访问端口，留空自动分配   | 否  |
| enabled     | Boolean | true      | 是否开启代理          | 否  |

