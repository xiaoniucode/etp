---
sidebar_position: 1
---

参考案例：

```toml
[[proxies]]
name = "MySQL"
protocol = "tcp"
remote_port = 3307
targets = [
    { host = "127.0.0.1", port = 3306, weight = 1, name = "mysql-instance-1" }
]

[[proxies]]
name = "Redis"
protocol = "tcp"
remote_port = 6380
targets = [
    { host = "127.0.0.1", port = 6379, weight = 1, name = "redis-instance-1" }
]
```

## 参数说明

| 参数名         | 类型                  | 默认值  | 描述                      | 必填 |
|:------------|:--------------------|:-----|:------------------------|:---|
| name        | String              | -    | 代理名称，唯一标识一个代理配置         | 是  |
| protocol    | String              | -    | 协议类型                    | 是  |
| remote_port | Integer             | -    | 远程访问端口，监听端口，没有填会自动分配    | 否  |
| targets     | Array&lt;Object&gt; | -    | 目标服务列表，内网真实服务列表，支持单机和集群 | 是  |
| enabled     | Boolean             | true | 是否开启代理                  | 否  |

### targets 子参数说明

| 参数名    | 类型      | 默认值       | 描述                | 必填 |
|:-------|:--------|:----------|:------------------|:---|
| host   | String  | 127.0.0.1 | 目标服务主机地址          | 否  |
| port   | Integer | -         | 目标服务端口            | 是  |
| weight | Integer | 1         | 负载均衡权重，用于权重负载均衡算法 | 否  |
| name   | String  | -         | 目标服务名称            | 否  |
