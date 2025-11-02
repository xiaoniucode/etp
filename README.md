[![GitHub Stars](https://img.shields.io/github/stars/xiaoniucode/etp?style=for-the-badge&logo=github)](https://github.com/xiaoniucode/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xiaoniucode/etp?style=for-the-badge&logo=github)](https://github.com/xiaoniucode/etp)
[![Open Issues](https://img.shields.io/github/issues/xiaoniucode/etp?style=for-the-badge)](https://github.com/xiaoniucode/etp/issues)
[![License](https://img.shields.io/github/license/xiaoniucode/etp?style=for-the-badge)](https://github.com/xiaoniucode/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xiaoniucode/etp?style=for-the-badge)](https://github.com/xiaoniucode/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)

## ✨ Introduction
**etp** (Easy Tunnel Proxy) is a lightweight, high-performance tunnel proxy middleware that supports TCP, HTTP protocols, and upper-layer TCP protocols with TLS 1.3 encryption. It enables quick exposure of internal network services to the public internet, facilitating development and testing while reducing the need for cloud server purchases.

📄 [Visit Documentation Site](https://xiaoniucode.github.io/etp)

## 🌟 Features
- 💻 Supports TCP, HTTP/HTTPS protocols
- 🔐 Utilizes efficient TLS 1.3 encryption
- 🛜 High-performance data transmission
- 🚀 Millisecond-level startup
- 🔗 Connection re-establishment
- 🔐 Authentication
- 🐒 Multi-client support
- 🧿 Automatic mapping port allocation
- 📺 Cross-platform support, including arm64 and amd64 architectures
- 💨 Lightweight with low resource usage

## 🚀 Quick Start
Download the appropriate [release package](https://github.com/xiaoniucode/etp/releases) for your operating system. The server is typically deployed on a machine with a public IP address.

### 🖥️ Server
This example demonstrates how to expose an internal MySQL service to port 3307 on the public internet.

> Edit the configuration file `etps.toml` with the following content:

```toml 
bindPort=9527
[[clients]]
name = "Mac" # Client name
secretKey = "your-client-auth-key" #[Required] Custom 32-bit key

[[clients.proxies]]
name = "mysql" #[Optional] Service name
type = "tcp" #[Required] Network protocol
localPort = 3306 #[Required] Internal service port
remotePort = 3307 #[Optional] Public service port; if not specified, a random port will be assigned
```

Start the etp server on a Linux server with a public IP address for external access.

```shell
nohup ./etps -c etps.toml &
```

### 💻 Client (etpc) Configuration

> Edit the configuration file `etpc.toml`:

```toml
serverAddr = "x.x.x.x" # Server IP address where etps is deployed
serverPort=9527 # Server's bindPort
secretKey="your-client-auth-key" # Must match the server configuration
```

Start the client on the internal network computer, using a Unix-based system as an example:

```shell
./etpc -c etpc.toml # Or run in the background: nohup ./etpc -c etpc.toml &  
```

🔔 **Note**: If the configuration file is in the same directory as the executable, the **-c** flag is not required.

After successful startup, connect to MySQL using port **3307**.

## 🔒 SSL Configuration (Optional)

1️⃣ First, download the certificate generation command-line tool [generate_ssl_cert.sh](scripts/generate_ssl_cert.sh) to your local machine (alternatively, use JDK's keytool). For detailed usage, refer to the [certificate generation documentation](doc/code-gen.md). This tool currently requires a JDK environment.

2️⃣ After downloading the script, execute the following command to generate certificates and keys. If you prefer simplicity, run the script without parameters to automatically generate complex keys.

```shell
sudo sh cert-gen.sh -serverStorePass s123456 -clientStorePass c123456 -keypass k123456
```
![cert-gen-en.png](doc/image/cert/cert-gen-en.png)
3️⃣ The script generates two key certificate files: **server.p12** for the server and **client.p12** for the client. Configure these in the respective toml files.

![result.png](doc/image/cert/result.png)

- Add the following to the `etps.toml` configuration file:

```properties
ssl = true
[keystore]
path = "server.p12" # Server certificate path
keyPass = "veHzlXBADiAr" # Private key
storePass = "veHzlXBADiAr" # Server keystore password
```

- Add the following to the `etpc.toml` configuration file:

```properties
ssl = true
[truststore]
path = "client.p12" # Certificate path
storePass = "JCAkB4X7G3T6" # Client keystore password
```

> ⚠️ **Warning**: If `ssl` is set to `true`, both server and client must enable SSL, or an error will occur!

For more details, refer to the [certificate configuration documentation](doc/code-gen.md).

## Problem Reporting
Submit issues: [issues](https://github.com/xiaoniucode/etp/issues)

## 📈 Project Trends
<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
