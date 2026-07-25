---
sidebar_position: 1
title: 域名路由
description: 域名绑定与虚拟主机路由
---

# 自定义域名

HTTP / HTTPS / 文件共享通过域名做虚拟主机路由。域名类型优先级：`custom_domains` -> `sub_domains` -> `auto_domain`。

## 服务端配置

先配置根域名，供子域名与自动生成使用：

```toml
# orbien-server.toml
root_domains = ["domain.com"]
```

## 客户端配置

### 自定义域名

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_ip = "127.0.0.1"
local_port = 8001
custom_domains = ["web.domain.com", "api.domain.com"]
```

### 子域名

在服务端 `root_domains` 下绑定前缀，例如 `web` -> `web.domain.com`：

```toml
[[proxies]]
name = "web"
protocol = "http"
local_ip = "127.0.0.1"
local_port = 8001
sub_domains = ["web", "api"]
```

### 自动生成

未配置 `custom_domains` / `sub_domains`，且 `auto_domain = true` 时，从根域名随机生成一级子域名：

```toml
[[proxies]]
name = "web"
protocol = "http"
local_ip = "127.0.0.1"
local_port = 8001
auto_domain = true
```

## 参数说明

### 服务端

| 参数名          | 类型    | 默认值 | 描述               | 必填 |
|:-------------|:------|:----|:-----------------|:---|
| root_domains | Array |     | 根域名列表，用于子域名与自动生成 | 否  |

### 客户端

| 参数名            | 类型      | 默认值  | 描述                            | 必填 |
|:---------------|:--------|:-----|:------------------------------|:---|
| custom_domains | Array   |      | 完整自定义域名列表                     | 否  |
| sub_domains    | Array   |      | 子域名前缀列表，需配合服务端 `root_domains` | 否  |
| auto_domain    | Boolean | true | 未配置域名时是否自动生成                  | 否  |

:::info 说明
自动生成与子域名依赖服务端已配置 `root_domains`，否则会注册失败。
:::
