---
sidebar_position: 7
title: Header 改写
description: HTTP / HTTPS 请求与响应头的 set / add / remove
---

# Header 改写

为 HTTP / HTTPS 代理改写请求头或响应头。系统会先注入 `X-Forwarded-For`，再应用自定义规则。

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_ip = "127.0.0.1"
local_port = 8001
custom_domains = ["web.domain.com"]

[proxies.header_rewrite]
enabled = true

[[proxies.header_rewrite.request]]
action = "set"
name = "X-Real-IP"
value = "$client_ip"

[[proxies.header_rewrite.request]]
action = "add"
name = "X-Request-Id"
value = "$request_id"

[[proxies.header_rewrite.response]]
action = "remove"
name = "Server"
```

## 参数说明

| 参数名      | 类型      | 默认值   | 描述         | 必填 |
|:---------|:--------|:------|:-----------|:---|
| enabled  | Boolean | false | 是否开启 Header 改写 | 否  |
| request  | Array   |       | 请求头改写规则列表  | 否  |
| response | Array   |       | 响应头改写规则列表  | 否  |

### 规则

| 参数名    | 类型     | 默认值 | 描述                              | 必填 |
|:-------|:-------|:----|:--------------------------------|:---|
| action | String |     | 动作：`set` / `add` / `remove`     | 是  |
| name   | String |     | Header 名称                       | 是  |
| value  | String |     | Header 值；`remove` 时无需填写         | 否  |

### 动作说明

| 动作     | 描述                |
|:-------|:------------------|
| set    | 设置或覆盖该 Header     |
| add    | 仅当该 Header 不存在时添加 |
| remove | 删除该 Header        |

### 可用变量

`value` 中可使用以下变量：

| 变量           | 描述     |
|:-------------|:-------|
| `$client_ip` | 访客真实 IP |
| `$scheme`    | 请求协议   |
| `$host`      | 请求 Host |
| `$request_id` | 请求唯一 ID |

:::info 说明
- 请求头与响应头规则合计最多 64 条
- 禁止改写：`Content-Length`、`Transfer-Encoding`、`Connection`、`Keep-Alive`、`Upgrade`
:::
