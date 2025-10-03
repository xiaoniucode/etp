[![GitHub Stars](https://img.shields.io/github/stars/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![Open Issues](https://img.shields.io/github/issues/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/issues)
[![License](https://img.shields.io/github/license/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)
## 介绍
  **etp**（Easy Tunnel Proxy）是一个轻量级的高性能隧道代理中间件，支持TCP、HTTP协议以及TCP上层协议。可将内网服务快速暴露为公网服务，便于开发测试，减少购买云服务器成本。
## 功能特性
- 支持TCP、HTTP、HTTPS等协议
- 高性能数据传输
- 毫秒级启动
- 支持多客户端
- 轻量级，使用简单
## 快速开始
根据操作系统下载对应的[发行版本](https://github.com/xilio-dev/etp/releases)安装包，服务端一般部署在具备公网IP的服务器上。
### 服务端
>编辑配置文件 etps.toml
```toml 
bindPort=9527
[[clients]]
name = "Mac"
secretKey = "4b0063baa5ae47c2910fc25265aae4b9"

[[clients.proxies]]
name = "mysql"
type = "tcp"
localPort = 3306
remotePort = 3307

[[clients.proxies]]
name = "redis"
type = "tcp"
localPort = 6379
remotePort = 6380
```
启动etp服务端，若需要外部访问，需要部署在具备公网IP的服务器上。
```shell
./etps -c etps.toml
```
### 客户端etpc配置
>编辑配置文件 etpc.toml 

```toml
serverAddr = "127.0.0.1"
serverPort=9527
secretKey="4b0063baa5ae47c2910fc25265aae4b9"
```
在内网电脑启动客户端
```shell
./etpc -c etpc.toml
```
## 项目趋势
<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>

