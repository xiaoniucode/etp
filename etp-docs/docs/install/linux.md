---
sidebar_position: 1
---

# Linux

## 一、采用nohup
```js
etps.toml

bindPort = 9527
```

```js
etpc.toml

serverAddr = "x.x.x.x"
serverPort = 9527
secretKey = "your secret key"
```
将可执行程序和配置文件放在同一个文件夹，然后：
```js
sudo nohup ./etps & #服务端
sudo nohup ./etps & #客户端
```
或者
```js
sudo nohup ./etps -c ./etps.toml & #服务端
sudo nohup ./etps -c ./etpc.toml & #客户端
```

- -c: 用于指定配置文件所在的路径。
