[![GitHub Stars](https://img.shields.io/github/stars/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![Open Issues](https://img.shields.io/github/issues/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/issues)
[![License](https://img.shields.io/github/license/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)

## Introduction

**etp** (Easy Tunnel Proxy) is a lightweight, high-performance tunneling proxy middleware that supports TCP, HTTP protocols, and upper-layer protocols over TCP. It allows you to quickly expose internal network services as public network services, which is convenient for development and testing and reduces the need to purchase cloud servers.

## Features

- Supports TCP, HTTP, HTTPS, and other protocols
- High-performance data transmission
- Multi-client support
- Lightweight and easy to use

## Quick Start

Make sure both the server and client have JDK8 or above installed.

### Server

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

Start the server on a public network server:

```shell
java -jar etps.jar -c etps.toml
```

### Client `etpc` Configuration

> Edit the configuration file `etpc.toml`

```toml
serverAddr = "127.0.0.1"
serverPort=9527
secretKey="4b0063baa5ae47c2910fc25265aae4b9"
```

Start the client on your internal network machine:

```shell
java -jar etpc.jar -c etpc.toml
```

## Project Trends

<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>
