---
sidebar_position: 7
---

# SpringBoot生态集成
`etp v0.4.0`版本开始支持`etp客户端`嵌入到`SpringBoot` / `Spring Cloud`应用中运行，只需引入`Maven`坐标，添加几行配置即可无缝集成，生命周期同`SpringBoot`应用，无需独立安装运行`etpc`客户端。

## 引入依赖
根据自己项目使用的`SpringBoot`版本引入对应版本依赖，最新版本去[Maven中央仓库](https://central.sonatype.com/)获取，同时仓库也提供了对应版本的使用案例，[下载地址](https://github.com/xiaoniucode/etp/exampls)。
### SpringBoot 2.x

```xml
<dependency>
    <groupId>io.github.xiaoniucode</groupId>
    <artifactId>etp-spring-boot-starter</artifactId>
    <version>版本号</version>
</dependency>
```

### SpringBoot 3.x

```xml
<dependency>
    <groupId>io.github.xiaoniucode</groupId>
    <artifactId>etp-spring-boot3-starter</artifactId>
    <version>版本号</version>
</dependency>
```

### SpringBoot 4.x

```xml
<dependency>
    <groupId>io.github.xiaoniucode</groupId>
    <artifactId>etp-spring-boot4-starter</artifactId>
    <version>版本号</version>
</dependency>
```
## 快速开始
在`application.yml`中添加如下最基础的配置即可开启代理。
```yaml
etp:
  client:
    enabled: true #是否开启etp代理，默认为关闭状态
    server-addr: localhost #服务器地址，默认：localhost
    secret-key: your-secret-key #认证Token
```
启动`SpringBoot`应用后查看`控制台日志`可看到公网访问链接相关信息。

**完整配置参考：**

```yaml
etp:
  client:
    enabled: true #是否开启etp代理，默认为关闭状态
    server-addr: localhost #服务器地址
    secret-key: 430299890d2b4376a788ecbf88090810 #认证Token
    local-host: 127.0.0.1 #内网IP
    remote-port: 8025 #自定义remotePort公网端口，不指定会自动分配
    auto-start: true # 是否自动开启代理，默认是true，如果为false需要去管理界面手动开启
    tls: false # 是否开启隧道tls安全加密
    truststore:
      path: cert/client.p12 #证书路径
      store-pass: your-store-pass # 密钥
    initial-delay-sec: 2 #首次连接失败延迟
    max-delay-sec: 10 # 最大延迟秒数
    max-retries: 8 # 最大重试次数
```
详细配置细节请查看源码: `EtpClientProperties.java`
