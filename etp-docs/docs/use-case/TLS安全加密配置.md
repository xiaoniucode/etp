---
sidebar_position: 3
---

:::tip 提示
etp强制采用`TLSv1.3`协议
:::

本文以Linux/MacOS操作系统为例介绍如何配置TLS，实现数据加密传输。

## 密钥生成
- 下载对应操作系统的密钥生成工具: [keytool](https://github.com/xiaoniucode/etp/tree/main/scripts/tls)
- 下载自动生成密钥命令行脚本：[tls.sh](https://github.com/xiaoniucode/etp/tree/main/scripts/tls/tls.sh)

将上面下载的两个文件都放在同一个文件夹，去掉keytool的后缀（如keytool_arm，去掉_arm），然后运行脚本文件快速生成密钥配置
```shell
sh tls.sh 
```

输入y以后会自动生成服务端和客户端的密钥和证书
![tls_1.png](img/tls_1.png)
对应的密钥配置和证书也会在当前目录cert下生成。
![tls_2.png](img/tls_2.png)


## ✍️配置

将上面生成的证书和密钥分别客户端和服务端进行配置。

![tls_res.png](img/tls_res.png)

### 🖥️服务端
```js
ssl = true
[keystore]
path = "/path/cert/server.p12"
keyPass = "your-keypass"
storePass = "your-storepass"
```
### 💻客户端
```js
ssl = true
[truststore]
path = "/path/cert/client.p12"
storePass = "your-storepass"
```
⚠️ `tls=true / tls=false`必须在服务端和客户端同时设置并保持一致！

---

`tls.sh`脚本详细用法可以加上`-h`参数查看

![tls_help.png](img/tls_help.png)
