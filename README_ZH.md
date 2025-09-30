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
- 支持多客户端
- 轻量级，使用简单
## 安装
代理服务器的安装可以采用nohup、docker、定义服务等方式安装，下文简单介绍一下服务的启动和使用。
### 服务端etps配置
>etps.toml
```toml 
bindPort=9527
[[clients]]
name = "Mac"
secretKey = "4b0063baa5ae47c2910fc25265aae4b9"
status = 1 

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
启动服务端
```shell
java -jar etps.jar -c etps.toml
```
### 客户端etpc配置
>etpc.toml 

```toml
serverAddr = "127.0.0.1"
serverPort=9527
secretKey="4b0063baa5ae47c2910fc25265aae4b9"
```
启动客户端
```shell
java -jar etpc.jar -c etpc.toml
```
## 项目趋势
<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>

