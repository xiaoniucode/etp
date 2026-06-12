<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<p align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
  一个高性能的内网穿透平台
</p>
<div align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://img.shields.io/github/stars/xiaoniucode/etp?style=for-the-badge&logo=github" alt="GitHub Stars"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp/forks">
    <img src="https://img.shields.io/github/forks/xiaoniucode/etp?style=for-the-badge&logo=github" alt="GitHub Forks"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/xiaoniucode/etp?style=for-the-badge" alt="License"/>
  </a>
 <a href="https://github.com/xiaoniucode/etp/releases/v0.10.0">
    <img src="https://img.shields.io/badge/etp-0.10.0-blue?style=for-the-badge" alt="etp:0.10.0"/>
  </a>
<a href="https://somsubhra.github.io/github-release-stats/?username=xiaoniucode&repository=etp">
  <img src="https://img.shields.io/github/downloads/xiaoniucode/etp/total?style=for-the-badge" alt="Downloads"/>
</a>

</div>

<div align="center">
  <a href="README.md"><strong>README</strong></a> &nbsp;|&nbsp;
  <a href="README_ZH.md"><strong>简体中文</strong></a> &nbsp;|&nbsp;
  <a href="https://xiaoniucode.github.io/etp"><strong>文档网站</strong></a>
</div>

![dashboard.png](doc/image/dashboard.png)

## 介绍
**etp (Easy Tunnel Proxy)** 是一个高性能的**内网穿透平台**。
- 支持 TCP、HTTP协议代理
- 数据压缩
- mTLS 传输加密
- IP CIDR 访问控制（白/黑名单）
- HTTP Basic Auth 鉴权认证
- Token身份认证
- 精细化带宽限流
- 负载均衡，支持集群代理
- 域名、子域名支持
- 内置Web UI管理面板
- Spring Boot 集成与 SDK 支持
- 兼容Windows、Linux、macOS

## 快速开始

### 安装服务端

环境要求：

- Docker 20+
- Linux x86_64

Docker一键启动`etps`服务端:

```shell
curl -fsSL https://raw.githubusercontent.com/xiaoniucode/etp/main/scripts/install.sh -o install.sh
chmod +x install.sh
sudo sh install.sh
```

管理面板访问地址：`http://服务器IP:8020` (admin: 123456)
### 安装客户端

从 [GitHub Releases](https://github.com/xiaoniucode/etp/tags)页面下载最新版本，根据您的操作系统下载对应的二进制文件。

下载到本地解压后编辑配置文件`etpc.toml`，

```toml
server_addr = "etps所在服务IP或域名"
[auth]
token = "身份认证令牌"
```

运行客户端:

```shell
./etpc -c etpc.toml # Linux / MacOS

etpc.exe -c etpc.toml # Windows
```

更多使用细节请查阅[文档网站](https://xiaoniucode.github.io/etp/)。

## 问题反馈

反馈问题:[issues](https://github.com/xiaoniucode/etp/issues)

## 项目趋势

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
