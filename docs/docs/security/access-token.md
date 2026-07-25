---
sidebar_position: 1
title: 身份认证
description: 客户端接入鉴权
---

# 身份认证

访问令牌用于客户端接入服务端。服务端可配置多个静态 Token，客户端使用其中一个完成认证。

## 服务端配置

```toml
# orbien-server.toml
[[auth.tokens]]
name = "win"
token = "TOKEN_VALUE"

[[auth.tokens]]
name = "mac"
token = "TOKEN_VALUE"
```

## 客户端配置

```toml
# orbien.toml
[auth]
token = "TOKEN_VALUE"
name = "my-agent"
```

## 参数说明

### 服务端 tokens

| 参数名   | 类型     | 默认值 | 描述        | 必填 |
|:------|:-------|:----|:----------|:---|
| name  | String |     | Token 名称  | 是  |
| token | String |     | Token 值，需唯一 | 是  |

### 客户端 auth

| 参数名   | 类型     | 默认值        | 描述                 | 必填 |
|:------|:-------|:-----------|:-------------------|:---|
| token | String |            | 与服务端匹配的 Token      | 是  |
| name  | String | 本机主机名      | 客户端显示名称            | 否  |
