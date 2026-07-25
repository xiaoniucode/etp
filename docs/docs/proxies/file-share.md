---
sidebar_position: 6
title: 文件共享
description: 基于隧道的文件门户、鉴权、限流与预览
---

# 文件共享

将客户端本地目录通过隧道暴露为 Web 文件门户。固定走 HTTPS，明文访问会自动跳转。

## 服务端配置

```toml
# orbien-server.toml
server_addr = "0.0.0.0"
server_port = 9527
https_proxy_port = 8443
```

## 客户端配置

```toml
# orbien.toml
[[proxies]]
name = "home-nas"
protocol = "file"
root_path = "/file/share"
custom_domains = ["nas.domain.com"]

[proxies.file_auth]
enabled = true
users = [
    { user = "guest", pass = "111", permission = "read" },
    { user = "admin", pass = "111", permission = "read_write" }
]

[proxies.file_limits]
max_upload_size = "5MB"
allow_upload = true
allow_delete = true
allow_mkdir = true
allow_move = true
allow_rename = true
```

浏览器访问：https://nas.domain.com:8443

:::tip
更多域名配置细节请移步 [域名路由](../networking/domain.md)
:::

## 参数说明

| 参数名            | 类型      | 默认值  | 描述           | 必填 |
|:---------------|:--------|:-----|:-------------|:---|
| name           | String  |      | 代理名称         | 是  |
| protocol       | String  |      | 协议类型         | 是  |
| root_path      | String  |      | 共享根目录绝对路径    | 是  |
| custom_domains | Array   |      | 自定义域名列表      | 否  |
| sub_domains    | Array   |      | 子域名列表        | 否  |
| auto_domain    | Boolean | true | 未配置域名时是否自动生成 | 否  |
| enabled        | Boolean | true | 是否开启代理       | 否  |

## 授权认证

| 参数名     | 类型      | 默认值  | 描述          | 必填 |
|:--------|:--------|:-----|:------------|:---|
| enabled | Boolean | true | 是否开启用户名密码认证 | 否  |
| users   | Array   |      | 认证用户列表      | 否  |

### 用户信息

| 参数名        | 类型     | 默认值         | 描述                        | 必填 |
|:-----------|:-------|:------------|:--------------------------|:---|
| user       | String |             | 用户名                       | 是  |
| pass       | String |             | 密码                        | 是  |
| permission | String | read_write  | 权限：`read` / `read_write` | 否  |

:::info 说明
用户密码会被加密存储
:::

## 访问限制

| 参数名             | 类型      | 默认值   | 描述                | 必填 |
|:----------------|:--------|:------|:------------------|:---|
| max_upload_size | String  | 500MB | 单文件上传大小上限，如 `5MB` | 否  |
| allow_upload    | Boolean | true  | 是否允许上传            | 否  |
| allow_delete    | Boolean | true  | 是否允许删除            | 否  |
| allow_mkdir     | Boolean | true  | 是否允许新建目录          | 否  |
| allow_move      | Boolean | true  | 是否允许移动            | 否  |
| allow_rename    | Boolean | true  | 是否允许重命名           | 否  |

## TLS 证书

可选。用于为该代理绑定独立证书；未配置时使用服务端默认证书。

| 参数名       | 类型     | 默认值 | 描述     | 必填 |
|:----------|:-------|:----|:-------|:---|
| cert_file | String |     | 证书文件路径 | 是  |
| key_file  | String |     | 私钥文件路径 | 是  |
