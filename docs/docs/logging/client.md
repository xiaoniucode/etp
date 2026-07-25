---
sidebar_position: 2
title: 客户端日志
---

# 客户端日志

## 配置示例

```toml
# orbien.toml
[log]
level = "info"
path = "logs"
name = "orbien.log"
archive_pattern = "orbien.%d{yyyy-MM-dd}.log"
log_pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
max_history = 7
total_size_cap = "10MB"
```

## 参数说明

| 参数名             | 类型      | 默认值                         | 描述                                                    |
|:----------------|:--------|:----------------------------|:------------------------------------------------------|
| level           | String  | info                        | 根日志级别，如 `trace` / `debug` / `info` / `warn` / `error` |
| path            | String  | log                         | 日志目录                                                  |
| name            | String  | orbien.log                  | 当前写入的日志文件名                                            |
| archive_pattern | String  | orbien.%d\{yyyy-MM-dd\}.log | 按天滚动的归档文件名模式（相对 `path`）                               |
| log_pattern     | String  | 见上例                         | 单行日志格式（Logback Pattern）                               |
| max_history     | Integer | 30                          | 归档保留天数                                                |
| total_size_cap  | String  | 3GB                         | 日志总占用上限，如 `50MB`、`3GB`                                |
