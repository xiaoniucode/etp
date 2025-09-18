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
## 介绍
  etp 是一个内网穿透工具，用于将内网服务暴露到公网，提供外部服务，减少云服务器成本，支持TCP、HTTP等协议的穿透.

## 功能特性
- 支持TCP、UDP、HTTP、HTTPS等协议
- 安全加密通信
- 交互式终端管理
- 多客户端支持
## 安装

代理服务器的安装可以采用nohup、docker、定义服务等方式安装，下文简单介绍一下服务的启动和使用。

### 服务端
⚠️ 当前版本不支持管理界面，需手动配置代理规则
修改application.yml也可以采用默认的，需要保证proxy-path目录有访问权限。
```yaml
etp:
  bind-port: 8523 #隧道监听端口，用于与客户端进行通信
  #代理规则配置文件存储地址,需要自己配置
  proxy-path: /home/proxy.toml

```
下面是`proxy.toml`代理规则配置案例：
```toml
[[clients]]
name = "客户端1" #客户端的名字 可以当作一个用户
secretKey = "4b0063baa5ae47c2910fc25265aae4b9" #客户端秘钥
status = 1 #客户端状态 1:启用 0:禁用

[[clients.proxies]]
name = "mysql" #代理名称 自定义
type = "tcp" #代理类型
localIP = "127.0.0.1" #内网IP地址 localhost/192.168.x.x/127.0.0.1
localPort = 3306 #内网服务的端口
remotePort = 3307 #暴露在公网的端口

[[clients.proxies]]
name = "redis"
type = "tcp"
localIP = "127.0.0.1"
localPort = 6379
remotePort = 6380
```
启动服务端：
```shell
java -jar etp-server-1.0.jar
```
### 客户端
> 客户端支持启动多个，每个客户端通过secretKey密钥进行标识。

客户端启动的时候需要通过`-c`指定一个配置文件`conf.toml`,名字不影响。
```toml
serverAddr = "127.0.0.1" #代理服务器IP地址，如果部署到公网，需要填写公网服务器的IP地址
serverPort=8523 #代理服务器隧道端口，和服务端配置bind-port对应
secretKey="4b0063baa5ae47c2910fc25265aae4b9" #认证密钥
```
启动客户端：
```shell
java -jar etp-client-1.0.jar -c conf.toml
```

## 开源许可

## 联系
Email：xilio1024@gmail.com
## 项目趋势

<p align="center">
  <a href="https://github.com/xilio-dev/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xilio-dev/etp&type=Date" alt="Star History">
  </a>
</p>

