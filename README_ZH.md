<div align="center">
  <img src="doc/logo.png" alt="Logo" width="180" height="180" style="border-radius:24px;margin-bottom:20px;"/>
</div>
<p align="center" style="font-size:18px;color:#555;margin-top:-10px;margin-bottom:24px;">
  由Netty驱动的内网穿透隧道
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
 <a href="https://github.com/xiaoniucode/etp/releases/v0.5.1">
    <img src="https://img.shields.io/badge/etp-0.5.1-blue?style=for-the-badge" alt="etp:0.5.1"/>
  </a>
<a href="https://somsubhra.github.io/github-release-stats/?username=xiaoniucode&repository=etp">
  <img src="https://img.shields.io/github/downloads/xiaoniucode/etp/total?style=for-the-badge" alt="Downloads"/>
</a>

</div>

<div align="center">
  <a href="README.md"><strong>README</strong></a> &nbsp;|&nbsp;
  <a href="README_ZH.md"><strong>简体中文</strong></a> &nbsp;|&nbsp;
  <a href="https://xiaoniucode.github.io/etp"><strong>文档地址</strong></a>
</div>

## 介绍

etp（Easy Tunnel Proxy）是一个跨平台的内网穿透工具，支持TCP协议以及TCP上层协议，支持TLS1.3高效安全加密协议，支持纯Toml静态配置或管理界面动态配置使用，可以很方便和SpringBoot生态集成。

## 功能特性

- 支持TCP协议以及TCP上层协议
- 主持TLS1.3高效安全加密协议
- 支持Spring Boot&Cloud生态无缝集成
- 内置易于操作的管理界面
- 高效数据传输
- 毫秒级启动
- 断线重连
- 身份认证
- 支持多客户端
- 自动分配映射端口
- 支持纯Toml配置或管理界面使用
- 跨平台，兼容arm64和amd64架构

## 面板截图

etp 除了支持纯`Toml`静态配置使用以外，还支持图形界面管理，管理使用成本低，功能更丰富。

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

## 快速开始

根据操作系统下载对应的[发行版本](https://github.com/xiaoniucode/etp/releases)安装包。若需要外部访问，需要部署在具备公网IP的服务器上，下面以本地环境为例介绍。

### 运行服务端

编辑配置文件 `etps.toml` 添加如下内容

```toml 
host="127.0.0.1"
bindPort = 9527
[[clients]]
name = "client"
secretKey = "认证密钥"

[[clients.proxies]]
name = "mysql"
type = "tcp"
localPort = 3306
remotePort = 3307
```

```shell
./etps -c etps.toml
```

### 运行客户端

编辑配置文件 `etpc.toml`

```toml
serverAddr = "127.0.0.1"
serverPort = 9527
secretKey = "认证密钥"
```

```shell
./etpc -c etpc.toml
```

## 问题反馈

反馈问题:[issues](https://github.com/xiaoniucode/etp/issues)

## 项目趋势

<p align="center">
  <a href="https://github.com/xiaoniucode/etp/stargazers">
    <img src="https://api.star-history.com/svg?repos=xiaoniucode/etp&type=Date" alt="Star History">
  </a>
</p>
