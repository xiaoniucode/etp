---
sidebar_position: 7
title: 连接池与重试
description: 客户端连接复用与断线重连
---

# 连接池与重试

## 连接池

预先建立隧道连接并复用，减少建连开销，加快后续代理流获取。

```toml
# orbien.toml
[connection.pool]
enabled = true

[connection.pool.multiplex]
plain = true
encrypt = true

[connection.pool.direct]
plain_count = 1
encrypt_count = 1
```

## 重试

控制客户端与服务端隧道断线后的重连策略。

```toml
# orbien.toml
[connection.retry]
initial_delay = 2
max_delay = 8
max_retries = 5
```

## 参数说明

### connection.retry

| 参数名           | 类型      | 默认值 | 描述        | 必填 |
|:--------------|:--------|:----|:----------|:---|
| initial_delay | Integer | 1   | 首次重试等待（秒） | 否  |
| max_delay     | Integer | 20  | 最大重试等待（秒） | 否  |
| max_retries   | Integer | 5   | 最大重试次数    | 否  |

### connection.pool

| 参数名     | 类型      | 默认值   | 描述      | 必填 |
|:--------|:--------|:------|:--------|:---|
| enabled | Boolean | false | 是否开启连接池 | 否  |

### connection.pool.multiplex

| 参数名     | 类型      | 默认值   | 描述         | 必填 |
|:--------|:--------|:------|:-----------|:---|
| plain   | Boolean | false | 预热明文多路复用连接 | 否  |
| encrypt | Boolean | false | 预热加密多路复用连接 | 否  |

### connection.pool.direct

| 参数名           | 类型      | 默认值 | 描述         | 必填 |
|:--------------|:--------|:----|:-----------|:---|
| plain_count   | Integer | 0   | 明文独立连接预热数量 | 否  |
| encrypt_count | Integer | 0   | 加密独立连接预热数量 | 否  |
