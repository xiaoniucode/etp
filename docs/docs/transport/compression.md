---
sidebar_position: 5
title: 数据压缩
description: Snappy / LZ4 / Zstd
---

# 数据压缩

对隧道帧数据压缩，在代理级开启。

```toml
# orbien.toml
[[proxies]]
name = "web"
protocol = "http"
local_port = 8001
custom_domains = ["web.domain.com"]

[proxies.transport]
compress = true
compress_algorithm = "snappy"
```

## 参数说明

| 参数名                | 类型      | 默认值    | 描述                             | 必填 |
|:-------------------|:--------|:-------|:-------------------------------|:---|
| compress           | Boolean | false  | 是否开启压缩                         | 否  |
| compress_algorithm | String  | snappy | 压缩算法：`snappy` / `lz4` / `zstd` | 否  |

## 压缩算法

| 算法     | 特点                | 适用场景              |
|:-------|:------------------|:------------------|
| snappy | 速度快、压缩比一般，CPU 开销低 | 默认选择，通用文本 / 协议流量  |
| lz4    | 压缩解压都很快，延迟低       | 对延迟敏感的实时流量        |
| zstd   | 压缩比更高，CPU 开销相对更大  | 带宽紧张、可接受一定 CPU 开销 |
