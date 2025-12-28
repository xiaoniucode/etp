---
sidebar_position: 4
---

## 介绍

`etps`服务端最新镜像仓库获取地址：[docker hub](https://hub.docker.com/r/xiaoniucode/etps)。

**端口说明：**
- `9527`是默认的`etpc`客户端与`etps`服务端通信的端口
- `8020`是默认的web管理界面端口

web管理界面默认登录用户名和密码是：`admin`: `etp.123456`

## 安装案例
:::tip 提示
启动容器时需要建立容器与服务器的端口映射，建议指定一个范围的端口(remotePort)。
:::

如果需要限制公网端口的分配范围，需要提前在指定映射目录的`etps.toml`文件中指定范围，案例如下：
```toml
[port_range]
start = 8600 #开始端口
end = 8609 #结束端口
```

### 1、快速体验
```powershell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 3306:3306 \
  -e ETP_DB_PATH=/app/data/etp.db \ 
  xiaoniucode/etps:latest
```

### 2、自定义JVM调优参数
```powershell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -e JVM_OPTS="-Xmx1g -Xms512m" \
  -e ETP_DB_PATH=/app/data/etp.db \ 
  xiaoniucode/etps:latest
```
- JVM_OPTS: 用于自定义JVM调优参数

### 3、配置volume，持久化数据

```powershell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -v /opt/etps/data:/app/data \
  -v /opt/etps/logs:/app/logs \
  -v /opt/etps/etps.toml:/app/etps.toml \
  -e ETP_DB_PATH=/app/data/etp.db \ 
  xiaoniucode/etps:latest
```

### 4、映射指定端口范围

```powershell
docker run -d \
  --name etps \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 8600-8609:8600-8609 \
  -e JVM_OPTS="-Xmx1g -Xms512m" \
  -e ETP_DB_PATH=/app/data/etp.db \ 
  xiaoniucode/etps:latest
```

### 5、docker-compose案例

```yaml
version: '3.8'

services:
  etps:
    image: xiaoniucode/etps:latest
    container_name: etps
    restart: always
    ports:
      - "8020:8020"     # Web管理界面
      - "9527:9527"     # 客户端通信端口
      - "8600-8609:8600-8609"  # 端口范围映射
    environment:
      - ETP_DB_PATH=/app/data/etp.db
      - JVM_OPTS=-Xmx1g -Xms1g #JVM调优参数
    volumes: # 数据持久化
      - opt/etps/data:/app/data \ # 数据
      - opt/etps/logs:/app/logs \ # 日志
      - opt/etps/etps.toml:/app/etps.toml \ # 配置文件
    networks:
      - etps-network

networks:
  etps-network:
    driver: bridge
```
