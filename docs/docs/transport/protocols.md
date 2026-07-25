---
sidebar_position: 2
title: 传输协议
description: 物理传输协议选择
---

# 传输协议

客户端与服务端之间的物理传输协议，可选 `tcp` / `websocket` / `quic`，不同协议适用于不同场景。

## 协议对比

| 协议        | 特点                       | 适用场景            |
|:----------|:-------------------------|:----------------|
| TCP       | 简单稳定，开销低，默认协议            | 内网或网络环境可控       |
| WebSocket | 基于 HTTP 升级，易穿越防火墙 / 反向代理 | 公网受限，仅放行 80/443 |
| QUIC      | 基于 UDP，建连快，弱网表现更好        | 高延迟、丢包较多的公网链路   |

## 服务端配置

```toml
# orbien-server.toml
server_port = 9527 # TCP

[transport.websocket]
enabled = true
server_port = 9528
path = "/tunnel"

[transport.quic]
enabled = true
server_port = 9529
```

## 客户端配置

### 全局配置

```toml
# orbien.toml
[transport]
protocol = "tcp"

```

### TCP 传输协议

默认传输协议，强制开启

```toml
server_addr = "127.0.0.1"
server_port = 9527
```

### WebSocket 传输协议

```toml
# orbien.toml
[transport.websocket]
server_port = 9528
path = "/tunnel"

[transport.quic]
server_port = 9529
```

### Quic 传输协议

```toml
# orbien.toml
[transport.quic]
server_port = 9529
```

## 代理级配置

可以在代理配置中覆盖全局配置

```toml
[proxies.transport]
protocol = "websocket"
```

## 参数说明

### 全局 protocol

| 参数名      | 类型     | 默认值 | 描述                                | 必填 |
|:---------|:-------|:----|:----------------------------------|:---|
| protocol | String | tcp | 传输协议：`tcp` / `websocket` / `quic` | 否  |

### WebSocket 

| 参数名            | 类型      | 默认值      | 描述             | 必填 |
|:---------------|:--------|:---------|:---------------|:---|
| enabled        | Boolean | false    | 服务端是否开启（仅服务端）  | 否  |
| server_port    | Integer | 9528     | 监听 / 连接端口      | 否  |
| path           | String  | /tunnel  | WebSocket 路径   | 否  |
| max_frame_size | Integer | 10485760 | 最大帧大小，字节（仅服务端） | 否  |

### QUIC 

| 参数名                      | 类型      | 默认值    | 描述            | 必填 |
|:-------------------------|:--------|:-------|:--------------|:---|
| enabled                  | Boolean | false  | 服务端是否开启（仅服务端） | 否  |
| server_port              | Integer | 9529   | 监听 / 连接端口     | 否  |
| max_idle_timeout_ms      | Integer | 120000 | 空闲超时（毫秒）      | 否  |
| initial_max_streams_bidi | Integer | 100    | 初始双向流数量上限     | 否  |

:::info 说明

- TCP 默认开启，端口为 `server_port`（默认 9527）
- QUIC 仅支持多路复用，不支持独立直连模式
  :::
