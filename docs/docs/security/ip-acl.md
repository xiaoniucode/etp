---
sidebar_position: 3
title: IP 访问控制
description: CIDR 黑白名单
---

# IP 访问控制

按访客 IP（支持 CIDR）限制代理访问，适用于各类代理协议。

```toml
# orbien.toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
local_ip = "127.0.0.1"
local_port = 3306
remote_port = 3307

[proxies.access_control]
enabled = true
mode = "allow"
allow = ["192.168.1.0/24", "10.0.0.1"]
deny = []
```

## 参数说明

| 参数名     | 类型      | 默认值   | 描述                             | 必填 |
|:--------|:--------|:------|:-------------------------------|:---|
| enabled | Boolean | false | 是否开启 IP 访问控制                   | 否  |
| mode    | String  |       | 控制模式：`allow`（白名单）/ `deny`（黑名单） | 是  |
| allow   | Array   |       | 允许的 IP / CIDR 列表               | 否  |
| deny    | Array   |       | 拒绝的 IP / CIDR 列表               | 否  |

:::info 说明

- `mode = "allow"`：仅允许命中 `allow` 的访客；`allow` 为空时拒绝全部
- `mode = "deny"`：拒绝命中 `deny` 的访客；`deny` 为空时允许全部
  :::
