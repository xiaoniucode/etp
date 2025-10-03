[![GitHub Stars](https://img.shields.io/github/stars/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![Open Issues](https://img.shields.io/github/issues/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/issues)
[![License](https://img.shields.io/github/license/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)
## Introduction
**etp** (Easy Tunnel Proxy) is a lightweight and high-performance tunnel proxy middleware that supports TCP, HTTP protocols, and upper-layer TCP protocols. It can quickly expose intranet services as public services, making it convenient for development and testing while reducing the cost of purchasing cloud servers.

## Features
- Support for TCP, HTTP, HTTPS, and other protocols
- High-performance data transmission
- Millisecond-level startup
- Multi-client support
- Lightweight and easy to use

## Quick Start
Download the corresponding [release version](https://github.com/xilio-dev/etp/releases) package for your operating system. The server is typically deployed on a machine with a public IP address.

### Server(etps)
> Edit the configuration file `etps.toml`
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

Start the `etp` server. For external access, deploy it on a server with a public IP address.
```shell
./etps -c etps.toml
```

### Client Configuration(etpc)
> Edit the configuration file `etpc.toml`
```toml
serverAddr = "127.0.0.1"
serverPort=9527
secretKey="4b0063baa5ae47c2910fc25265aae4b9"
```

Start the client on an intranet computer:
```shell
./etpc -c etpc.toml
```

## Project Trends
<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>
