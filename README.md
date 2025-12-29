<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<p align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
  An intranet penetration solution implemented based on Netty
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
 <a href="https://github.com/xiaoniucode/etp/releases/v0.5.0">
    <img src="https://img.shields.io/badge/etp-0.5.0-blue?style=for-the-badge" alt="etp:0.5.0"/>
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

## Introduction

etp(Easy Tunnel Proxy) is a cross-platform intranet penetration tool that supports the TCP protocol and TCP-based upper-layer protocols. It supports the TLS 1.3 high-efficiency and secure encryption protocol, supports pure Toml static configuration or dynamic configuration via a management interface, and can be easily integrated with the Spring Boot ecosystem.

## Features

- Supports TCP protocol and TCP-based upper-layer protocols  
- Supports the TLS 1.3 high-efficiency and secure encryption protocol  
- Supports seamless integration with the Spring Boot & Cloud ecosystem  
- Built-in easy-to-use management interface  
- High-efficiency data transmission  
- Millisecond-level startup  
- Automatic reconnection  
- Authentication  
- Supports multiple clients  
- Automatic allocation of mapping ports  
- Supports pure Toml configuration or usage via the management interface  
- Cross-platform, compatible with arm64 and amd64 architectures  

## Screenshots

In addition to supporting pure `Toml` static configuration, etp also supports graphical interface management, which has low usage cost and richer functionality.

<div align="center">
  <table width="100%">
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/登录.png" alt="Login" style="max-width:95%;height:auto;"/><br/>
        <sub>Login Page</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/监控面板.png" alt="Monitoring Dashboard" style="max-width:95%;height:auto;"/><br/>
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

## Quick Start

Download the corresponding [release package](https://github.com/xiaoniucode/etp/releases) according to your operating system. If external access is required, it needs to be deployed on a server with a public IP. The following example demonstrates usage in a local environment.

### Run the Server

Edit the configuration file `etps.toml`:

```toml 
host="127.0.0.1"
bindPort = 9527
[[clients]]
name = "client"
secretKey = "authentication key"

[[clients.proxies]]
name = "mysql"
type = "tcp"
localPort = 3306
remotePort = 3307
````

```shell
./etps -c etps.toml
```

### Run the Client

Edit the configuration file `etpc.toml`:

```toml
serverAddr = "127.0.0.1"
serverPort = 9527
secretKey = "authentication key"
```

```shell
./etpc -c etpc.toml
```

## Issue Feedback

Report issues: [issues](https://github.com/xiaoniucode/etp/issues)

## Project Trend

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
