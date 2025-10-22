[![GitHub Stars](https://img.shields.io/github/stars/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![Open Issues](https://img.shields.io/github/issues/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/issues)
[![License](https://img.shields.io/github/license/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)
## 介绍
  **etp**（Easy Tunnel Proxy）是一个轻量级的高性能隧道代理中间件，支持TCP、HTTP协议以及TCP上层协议且支持TLS1.3安全加密。用于将内网服务快速暴露为公网服务，便于开发测试，减少购买云服务器成本。
## 功能特性
- 💻支持TCP、HTTP、HTTPS等协议
- 🔐采用TLS1.3高效安全加密
- 🛜高性能数据传输
- 🚀毫秒级启动
- 🐒支持多客户端
- 💨轻量级，资源占用率低
## 快速开始
根据操作系统下载对应的[发行版本](https://github.com/xilio-dev/etp/releases)安装包，服务端一般部署在具备公网IP的服务器上。
### 服务端
这里演示如何将内网的MySQL服务暴露到公网的3307端口上。
>编辑配置文件 etps.toml添加如下内容
```toml 
bindPort=9527
[[clients]]
name = "Mac" #客户端名称
secretKey = "4b0063baa5ae47c2910fc25265aae4b9" #32位密钥请自定义，不要使用这个

[[clients.proxies]]
name = "mysql" #自定义一个名字
type = "tcp" #网络传输协议
localPort = 3306 #内网服务的端口
remotePort = 3307 #公网服务端口

```
在Linux服务器上启动etp服务端，若需要外部访问，需要部署在具备公网IP的服务器上。
```shell
nohup ./etps -c etps.toml &
```
### 客户端etpc配置
>编辑配置文件 etpc.toml 

```toml
serverAddr = "127.0.0.1"
serverPort=9527
secretKey="4b0063baa5ae47c2910fc25265aae4b9"
```
在内网电脑启动客户端，以mac为例
```shell
./etpc -c etpc.toml
```
启动成功后用***3307***端口去连接MySQL

## 项目趋势
<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>

