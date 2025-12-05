---
sidebar_position: 1
---

# Linux
`etp`服务端一般安装在具有`公网IP`的Linux服务器上，本文介绍Linux
## 方法一：采用nohup
服务端配置：
```js
etps.toml
host="0.0.0.0"
bindPort = 9527
```
客户端配置：
```js
etpc.toml

serverAddr = "x.x.x.x"
serverPort = 9527
secretKey = "your-secret-key"
```
将可执行程序和toml配置文件放在同一个文件夹，然后执行命令：
```js
sudo nohup ./etps & #启动服务端
sudo nohup ./etpc & #启动客户端
```
或者
```js
sudo nohup ./etps -c ./etps.toml & #启动服务端
sudo nohup ./etpc -c ./etpc.toml & #启动客户端
```
## 方法二：采用systemd
下文介绍如何使用systemd安装etp服务端。

1. 安装`systemd`
如果没有安装`systemd`，执行如下命令安装：
```js
# CentOS/RHEL
yum install systemd

# Debian/Ubuntu
apt install systemd
```
2. 创建`etps.service`服务文件
在`/etc/systemd/system` 目录下创建一个`etps.service`服务文件
```js
sudo vim /etc/systemd/system/etps.service
```
添加如下内容：
```js
[Unit]
# 服务名称，可自定义
Description = etp server
After = network.target syslog.target
Wants = network.target

[Service]
Type = simple
# 启动etps的命令，修改为自己的etps的文件路径
ExecStart = /path/to/etps -c /path/to/etps.toml

[Install]
WantedBy = multi-user.target
```
3. 使用`systemd`命令管理`etps`服务
```shell
# 启动
sudo systemctl start etps
# 停止
sudo systemctl stop etps
# 重启
sudo systemctl restart etps
# 查看状态
sudo systemctl status etps
```
## 方法三：采用Docker容器化部署

从发行版本中将下载下来的`jar`文件构建为`docker`镜像安装即可。
