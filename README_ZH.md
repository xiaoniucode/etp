
<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<p align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
  基于Netty实现的轻量级内网穿透反向代理应用
</p>
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
<a href="https://github.com/xiaoniucode/etp/releases">
  <img src="https://img.shields.io/github/downloads/xiaoniucode/etp/total?style=for-the-badge" alt="Downloads"/>
</a>

</div>

<div align="center">
  <a href="README.md"><strong>README</strong></a> &nbsp;|&nbsp;
  <a href="README_ZH.md"><strong>中文文档</strong></a> &nbsp;|&nbsp;
  <a href="https://xiaoniucode.github.io/etp"><strong>文档地址</strong></a>
</div>

## ✨ 介绍

**etp**（Easy Tunnel Proxy）是一个轻量级的高性能内网穿透反向代理应用，支持TCP、HTTP协议以及TCP上层协议，支持TLS1.3高效安全加密协议，支持纯Toml静态配置或管理界面动态配置使用。用于将内网服务快速暴露为公网服务，供公网用户访问以及开发测试，减少购买云服务器成本。
## 🌟 功能特性

- 📡 支持TCP、HTTP等协议
- 🔐 采用TLS1.3高效安全加密协议
- 🖥️ 提供易于操作的管理界面
- 🛜 高性能数据传输，延迟低
- 🚀 毫秒级启动
- 🔗 断线重连
- 🔐 身份认证
- 🐒 支持多客户端
- 🧿 自动分配映射端口
- 📄 支持纯Toml配置或管理界面使用
- 📺 跨平台，兼容arm64和amd64架构
- 💨 轻量级，资源占用率低

## 🎨 面板截图

etp 除了支持纯 Toml 静态配置使用以外，还支持图形化界面管理操作，管理使用成本低，功能也更丰富。

<div align="center">
  <table width="100%">
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/登录.png" alt="登录" style="max-width:95%;height:auto;"/><br/>
        <sub>登录界面</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/监控面板.png" alt="监控面板" style="max-width:95%;height:auto;"/><br/>
        <sub>监控面板</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/客户端列表.png" alt="客户端列表" style="max-width:95%;height:auto;"/><br/>
        <sub>客户端列表</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/添加映射2.png" alt="添加映射" style="max-width:95%;height:auto;"/><br/>
        <sub>添加映射</sub>
      </td>
    </tr>
    <tr>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/映射列表.png" alt="映射列表" style="max-width:95%;height:auto;"/><br/>
        <sub>映射列表</sub>
      </td>
      <td align="center" width="50%" style="vertical-align:top;">
        <img src="doc/image/screeshot/流量统计.png" alt="流量统计" style="max-width:95%;height:auto;"/><br/>
        <sub>流量统计</sub>
      </td>
    </tr>
  </table>
</div>

## 🚀 快速开始

根据操作系统下载对应的[发行版本](https://github.com/xiaoniucode/etp/releases)安装包，服务端一般部署在具备公网IP的服务器上。

### 🖥️ 服务端

这里演示如何将内网的MySQL服务暴露到公网的3307端口上。

> 编辑配置文件 `etps.toml` 添加如下内容

```toml 
bindPort = 9527
[[clients]]
name = "Mac" #[必填]自定义客户端名称
secretKey = "你的客户端认证密钥" #[必填]自定义密钥

[[clients.proxies]]
name = "mysql" #服务名字
type = "tcp" #[必填]网络传输协议
localPort = 3306 #[必填]内网服务的端口
remotePort = 3307 #[可选]公网服务端口，如果不填系统会随机分配一个端口
```

在Linux服务器上启动etp服务端，若需要外部访问，需要部署在具备公网IP的服务器上。

```shell
nohup ./etps -c etps.toml &
```

### 💻 客户端etpc配置

> 编辑配置文件 `etpc.toml`

```toml
serverAddr = "x.x.x.x" #etps部署服务器IP地址
serverPort = 9527 #服务端bindPort端口
secretKey = "你的客户端认证密钥" #和服务端配置保持一致
```

在内网电脑启动客户端，以unix操作系统为例

```shell
./etpc -c etpc.toml # 或后台运行：nohup ./etpc -c etpc.toml &  
```

🔔**备注**：如果配置文件和可执行程序在同一个文件夹可**不用用-c**指定配置。

启动成功后用 **3307** 端口去连接MySQL

## 🔒 配置TLS（可选）

1️⃣ 首先需要下载项目提供的证书生成命令行工具[tls.sh](scripts/tls/tls.sh)
到本地，根据自己的操作系统去[地址](scripts/tls)下载对应版本的keytool，去掉后缀后放到与tls.sh脚本同级目录。

2️⃣ 脚本下载本地后，在终端执行如下命令生成证书和密钥，脚本会自动生成密钥。(以Linux为例)

```shell
sudo sh tls.sh 
```

![tls_1.png](doc/image/cert/tls_1.png)

3️⃣ 脚本执行后会生成两个重要的证书文件，**server.p12** 需要部署到服务端，而 **client.p12** 部署在客户端，配置信息在对应的toml文件里。

![tls_2.png](doc/image/cert/tls_2.png)

- `etps.toml` 配置文件增加如下内容

```properties
tls=true
[keystore]
path="你的服务端证书路径"
keyPass="你的私钥"
storePass="你的存储库密钥"
```

- `etpc.toml` 配置文件需要增加如下内容

```properties
tls=true
[truststore]
path="你的客户端证书路径"
storePass="你的客户端存储库密钥"
```

> ⚠️ 如果tls设置为true，必须保证服务端和客户端都设置为true，否则会出错！


## 问题反馈

反馈问题:[issues](https://github.com/xiaoniucode/etp/issues)

## 📈 项目趋势

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
