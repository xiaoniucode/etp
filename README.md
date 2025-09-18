```text
▗▄▄▄▖▗▄▄▄▖▗▄▄▖ 
▐▌     █  ▐▌ ▐▌
▐▛▀▀▘  █  ▐▛▀▘ 
▐▙▄▄▖  █  ▐▌                                
```
[![GitHub Stars](https://img.shields.io/github/stars/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![GitHub Forks](https://img.shields.io/github/forks/xilio-dev/etp?style=for-the-badge&logo=github)](https://github.com/xilio-dev/etp)
[![Open Issues](https://img.shields.io/github/issues/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/issues)
[![License](https://img.shields.io/github/license/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/blob/main/LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/xilio-dev/etp?style=for-the-badge)](https://github.com/xilio-dev/etp/commits)

[README](README.md) | [中文文档](README_ZH.md)
## Overview
etp is an Intranet penetration tool used to expose Intranet services to the public network, provide external services, reduce the cost of cloud servers, and support the penetration of protocols such as TCP and HTTP.

## Features
- Supports protocols such as TCP, UDP, HTTP, HTTPS, etc
- Secure encrypted communication
- Interactive Terminal Management
- Multi client support
## Getting Started

The installation of a proxy server can be done through methods such as nohup, docker, or defining services. The following is a brief introduction to the startup and usage of services.

### Server
⚠️ The current version does not support the management interface and the proxy rules need to be manually configured
Modifying application.yml can also be done by default. It is necessary to ensure that the proxy-path directory has access rights.
```yaml
etp:
  bind-port: 8523 #Tunnel listening port, used for communication with clients
  #Proxy rule configuration file storage address, needs to be configured by oneself
  proxy-path: /home/proxy.toml

```
The following is a case of `proxy.toml` proxy rule configuration:
```toml
[[clients]]
name = "客户端1" #The name of the client can be considered as a user
secretKey = "4b0063baa5ae47c2910fc25265aae4b9" #Client key
status = 1 #Client status 1: enabled 0: disabled

[[clients.proxies]]
name = "mysql" #Proxy Name Customization
type = "tcp" #Agent Type
localIP = "127.0.0.1" #Internal IP address localhost/192.168.x.x/127.0.0.1
localPort = 3306 #Ports for internal network services
remotePort = 3307 #Ports exposed to the public network

[[clients.proxies]]
name = "redis"
type = "tcp"
localIP = "127.0.0.1"
localPort = 6379
remotePort = 6380
```
Start the server:
```shell
java -jar etp-server-1.0.jar
```
### Client
> The client supports launching multiple clients, each identified by a secretKey key.

When the client starts, a configuration file `conf.toml` needs to be specified through `-c`, and the name does not affect.
```toml
serverAddr = "127.0.0.1" #Proxy server IP address. If deployed to the public network, the IP address of the public network server needs to be filled in
serverPort=8523 #Proxy server tunnel port, corresponding to server configuration bind port
secretKey="4b0063baa5ae47c2910fc25265aae4b9" #Authentication Key
```
Start the client:
```shell
java -jar etp-client-1.0.jar -c conf.toml
```
## License

## Contact
Email：xilio1024@gmail.com
## Project Trends

<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>

