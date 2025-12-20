---
sidebar_position: 4
---

## 介绍

镜像已经发布到**docker hub**公共仓库，不用自行构建镜像，可以直接去[镜像仓库](https://hub.docker.com/r/xiaoniucode/etps)拉取安装。

**端口说明：**
- `9527`是默认的`etpc`客户端与`etps`服务端通信的端口
- `8020`是默认的web管理界面端口

web管理界面默认登录用户名和密码是：`admin`: `etp.123456`

## 安装
:::tip 提示
启动容器时需要建立容器与服务器的端口映射，建议制定一个范围的代理端口(remotePort)，避免重复启动容器。
:::

### 1、快速运行验证
```shell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 3306:3306 \
  xiaoniucode/etps:latest
```

### 2、自定义JVM调优参数
```shell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -e JVM_OPTS="-Xmx1g -Xms512m" \
  xiaoniucode/etps:latest
```
- JVM_OPTS: 用于自定义JVM调优参数

### 3、配置数据卷，持久化数据

```shell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -v /opt/etps/db:/app/etp.db \
  -v /opt/etps/logs:/app/logs \
  -v /opt/etps:/app/etps.toml \
  xiaoniucode/etps:latest
```

### 4、映射指定端口范围

```shell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 8600-8609:8600-8609 \
  -e JVM_OPTS="-Xmx1g -Xms512m" \
  xiaoniucode/etps:latest
```

### 5、docker-compose案例

```yaml
version: '3.8'

services:
  etps:
    image: xiaoniucode/etps:latest
    container_name: etps
    restart: unless-stopped
    ports:
      - "8020:8020"     # Web管理界面
      - "9527:9527"     # 客户端通信端口
      - "8600-8609:8600-8609"  # 端口范围映射
    environment:
      - JVM_OPTS=-Xmx1g -Xms512m #JVM调优参数
    volumes: # 数据持久化
      - ./etps/db:/app/etp.db \ # 数据
      - ./etps/logs:/app/logs \ # 日志
      - ./etps:/app/etps.toml \ # 配置文件
    networks:
      - etps-network

networks:
  etps-network:
    driver: bridge
```
