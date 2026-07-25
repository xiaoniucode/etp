---
sidebar_position: 4
title: 时间访问控制
description: 按星期与时段限制代理访客访问
---

# 时间访问控制

按星期与时段限制代理访客访问，适用于各类代理协议。

```toml
# orbien.toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
local_ip = "127.0.0.1"
local_port = 3306
remote_port = 3307

[proxies.time_access]
enabled = true
mode = "allow"
time_enabled = true
timezone = "Asia/Shanghai"
days = [1, 2, 3, 4, 5]
windows = [
    { start = "09:00:00", end = "18:00:00" }
]
```

## 参数说明

| 参数名          | 类型      | 默认值           | 描述                                   | 必填 |
|:-------------|:--------|:--------------|:-------------------------------------|:---|
| enabled      | Boolean | false         | 是否开启时间访问控制                           | 否  |
| mode         | String  | allow         | 控制模式：`allow`（仅窗口内可访问）/ `deny`（窗口内拒绝） | 否  |
| time_enabled | Boolean | true          | 是否启用时段窗口；关闭时仅按星期判断                   | 否  |
| timezone     | String  | Asia/Shanghai | 时区                                   | 否  |
| days         | Array   |               | 生效星期，`1-7` 表示`周一 - 周日`；为空表示每天        | 否  |
| windows      | Array   |               | 时段窗口列表，最多 16 个                       | 否  |

### 时间窗口

| 参数名   | 类型     | 默认值 | 描述                        | 必填 |
|:------|:-------|:----|:--------------------------|:---|
| start | String |     | 开始时间，`HH:mm` 或 `HH:mm:ss` | 是  |
| end   | String |     | 结束时间，`HH:mm` 或 `HH:mm:ss` | 是  |

:::info 说明

- `start` 与 `end` 不能相同；若 `start > end`，视为跨午夜窗口
- `mode = "allow"`：仅在选中星期且命中窗口时允许访问
- `mode = "deny"`：在选中星期且命中窗口时拒绝访问
  :::
