<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<div align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
A lightweight NAT traversal reverse proxy application implemented with Netty.
</div>
<div align="center">
  <a href="https://github.com/xiaoniucode/etp">
    <img src="https://img.shields.io/github/stars/xiaoniucode/etp?style=for-the-badge&logo=github" alt="GitHub Stars"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp">
    <img src="https://img.shields.io/github/forks/xiaoniucode/etp?style=for-the-badge&logo=github" alt="GitHub Forks"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp/issues">
    <img src="https://img.shields.io/github/issues/xiaoniucode/etp?style=for-the-badge" alt="Open Issues"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/xiaoniucode/etp?style=for-the-badge" alt="License"/>
  </a>
  <a href="https://github.com/xiaoniucode/etp/commits">
    <img src="https://img.shields.io/github/last-commit/xiaoniucode/etp?style=for-the-badge" alt="Last Commit"/>
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

## 🎨 面板截图

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

Download the appropriate [release](https://github.com/xiaoniucode/etp/releases) package for your OS. The server should run on a machine with a public IP address.

### 🖥️ Server

Here's how to expose an internal MySQL service to public port 3307.

> Edit your configuration file `etps.toml` and add:

```toml 
bindPort = 9527
[[clients]]
name = "Mac"             # [Required] Custom client name
secretKey = "YourSecret" # [Required] Custom secret key

[[clients.proxies]]
name = "mysql"           # Service name
type = "tcp"             # [Required] Protocol type
localPort = 3306         # [Required] Internal service port
remotePort = 3307        # [Optional] Public service port, auto-assigned if omitted
```

Start etp server on Linux; an external/public IP address is required for public access.

```shell
nohup ./etps -c etps.toml &
```

### 💻 Client (etpc) Configuration

> Edit your configuration file `etpc.toml`:

```toml
serverAddr = "x.x.x.x"   # IP address of the etps server deployment
serverPort = 9527        # bindPort from server configuration
secretKey = "YourSecret" # Must match the server configuration
```

Launch the client on your internal network machine (example for Unix):

```shell
./etpc -c etpc.toml        # Or run in background: nohup ./etpc -c etpc.toml &
```

🔔 **Note:** If the config file and executable are in the same folder, you can omit the `-c` flag.

Once started, use port **3307** to connect to your MySQL service.

## 🔒 TLS Configuration (Optional)

1️⃣ Download the project's certificate-generation script [generate_ssl_cert-en.sh](scripts/generate_ssl_cert-en.sh) (you may also use JDK's keytool). Usage details can be found in the [certificate generation guide](doc/code-gen.md). This script depends on JDK.

2️⃣ After downloading, generate certificates with:

```shell
sudo sh cert-gen.sh -serverStorePass s123456 -clientStorePass c123456 -keypass k123456
```

![cert-gen-1.png](doc/image/cert/cert-gen-en.png)

3️⃣ The script generates two main certificates: use **server.p12** for the server, and **client.p12** for the client. Configure their paths in your respective toml files.

![result.png](doc/image/cert/result.png)

- Add to `etps.toml`:

```toml
tls = true
[keystore]
path = "path/to/server.p12"
keyPass = "yourKeyPassword"
storePass = "yourStorePassword"
```

- Add to `etpc.toml`:

```toml
tls = true
[truststore]
path = "path/to/client.p12"
storePass = "yourClientStorePassword"
```

> ⚠️ If SSL/TLS is enabled, you must set it to `true` on both server and client, or otherwise it will not work!

For details, check the [certificate configuration guide](doc/code-gen.md).

## Issues and Feedback

Submit issues: [issues](https://github.com/xiaoniucode/etp/issues)

## 📈 Project Trends

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
