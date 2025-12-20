<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<div align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
A lightweight NAT traversal reverse proxy application implemented with Netty.
</div>
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
  <a href="https://github.com/xiaoniucode/etp/releases/v0.3.1">
    <img src="https://img.shields.io/badge/etp-0.3.1-blue?style=for-the-badge" alt="etp:0.3.1"/>
  </a>
<a href="https://somsubhra.github.io/github-release-stats/?username=xiaoniucode&repository=etp">
  <img src="https://img.shields.io/github/downloads/xiaoniucode/etp/total?style=for-the-badge" alt="Downloads"/>
</a>
</div>

<div align="center">
  <a href="README.md"><strong>README</strong></a> &nbsp;|&nbsp;
  <a href="README_ZH.md"><strong>中文文档</strong></a> &nbsp;|&nbsp;
  <a href="https://xiaoniucode.github.io/etp"><strong>Documentation</strong></a>
</div>

## ✨ Introduction

**etp** (Easy Tunnel Proxy) is a lightweight, high-performance reverse proxy application for NAT traversal. It supports TCP, HTTP and upper-layer TCP protocols, as well as efficient and secure TLS 1.3 encryption. Both pure Toml static configuration and dynamic configuration via management interface are supported. etp enables rapid exposure of internal network services to the public internet, making them accessible for public users and development/testing purposes, while reducing the cost of purchasing public cloud servers.

## 🌟 Features

- 📡 Supports TCP and HTTP protocols
- 🔐 Utilizes efficient and secure TLS 1.3 encryption
- 🖥️ Intuitive management UI for configuration
- 🛜 High-performance, low-latency data transfer
- 🚀 Millisecond-level startup
- 🔗 Automatic reconnection
- 🔐 Authentication
- 🐒 Supports multiple clients
- 🧿 Automatic port allocation for mapping
- 📄 Supports pure Toml configuration and web UI configuration
- 📺 Cross-platform, compatible with arm64 and amd64 architectures
- 💨 Lightweight with low resource consumption

## 🎨 Dashboard Screenshots

In addition to supporting pure Toml static configuration, etp also provides a graphical management interface for easy operation. This greatly reduces management and usage costs while offering more comprehensive features.

<div align="center">
  <table width="100%">
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/登录.png" alt="Login" style="max-width:95%;height:auto;"/><br/>
        <sub>Login Page</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/监控面板.png" alt="Dashboard" style="max-width:95%;height:auto;"/><br/>
        <sub>Monitoring Dashboard</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/客户端列表.png" alt="Client List" style="max-width:95%;height:auto;"/><br/>
        <sub>Client List</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/添加映射2.png" alt="Add Mapping" style="max-width:95%;height:auto;"/><br/>
        <sub>Add Mapping</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/映射列表.png" alt="Mapping List" style="max-width:95%;height:auto;"/><br/>
        <sub>Mapping List</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/流量统计.png" alt="Traffic Statistics" style="max-width:95%;height:auto;"/><br/>
        <sub>Traffic Statistics</sub>
      </td>
    </tr>
  </table>
</div>

## 🚀 Quick Start

Download the appropriate [release package](https://github.com/xiaoniucode/etp/releases) for your operating system. The server is typically deployed on a machine with a public IP address.

### 🖥️ Server Setup

This section demonstrates how to expose an internal MySQL service to the public on port 3307.

> Edit the configuration file `etps.toml` and add the following content:

```toml
bindPort = 9527
[[clients]]
name = "Mac" # [Required] Custom client name
secretKey = "your client authentication key" # [Required] Custom key

[[clients.proxies]]
name = "mysql" # Service name
type = "tcp" # [Required] Network transport protocol
localPort = 3306 # [Required] Internal service port
remotePort = 3307 # [Optional] Public service port; if not specified, a port will be randomly assigned by the system
```

Start the etp server on a Linux server. For external access, make sure to deploy it on a machine with a public IP address.

```shell
nohup ./etps -c etps.toml &
```

### 💻 Client etpc Configuration

> Edit the configuration file `etpc.toml`:

```toml
serverAddr = "x.x.x.x" # IP address of the server where etps is deployed
serverPort = 9527 # bindPort port of the server
secretKey = "your client authentication key" # Must match the server configuration
```

Start the client on an internal computer (Unix example):

```shell
./etpc -c etpc.toml # Or run in the background: nohup ./etpc -c etpc.toml &
```

🔔 **Note**: If the configuration file and executable are in the same folder, you **do not need to use -c** to specify the configuration.

After a successful startup, connect to MySQL using port **3307**.

## Feedback

To report issues: [issues](https://github.com/xiaoniucode/etp/issues)

Communication group：QQ-1061026558

<img src="doc/image/qq.JPG" alt="QQ交流群" width="200" height="200" />

## Future Work

- Natively supports HTTP/HTTPS protocols with port multiplexing
- IP Allowlist and Blocklist
- Rate Limiting

## 📈 Project Trends

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
