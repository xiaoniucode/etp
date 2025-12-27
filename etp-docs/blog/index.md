# 版本历史
详细发布细节请往Github [Release](https://github.com/xiaoniucode/etp/releases) 页面查看


## etp v0.5.0

### ✨新特性
- 支持更多日志自定义参数配置
- 管理界面增加服务端系统配置信息展示

### 🐛问题修复
- 修复通过SpringBoot starter启动客户端断线重连时，由于配置缺失导致无法连接的问题
- 修复端口池未在配置文件中指定范围时抛出异常的问题

### ⚙️ 配置变更
- 原`path`字段表示主日志文件的完整文件路径，现调整为仅表示基础日志目录路径
- 新增`name`字段，用于指定主日志文件名
- 原`pattern`字段表示完整的日志归档路径格式，现改为`archivePattern`，仅需填写归档文件名模式（无需包含完整路径）
- 部分字段是本次版本新增的配置参数

下面是完整的配置案例：

```toml
[log]
level = "info" # 日志级别
path = "log" # 基础路径
name = "etps.log" # 主日志文件名
archivePattern = "etpc.%d{yyyy-MM-dd}.log.gz" # 日志归档格式
logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n" # 控制台打印格式
maxHistory = 30 # 最大保存天数
totalSizeCap = "3GB" # 总空间大小
```

[查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.5.0)

## etp v0.4.1
### 🐛 问题修复
- 修复端口池没有设置范围时空指针异常
- 修复etp客户端启动成功回调接口空指针异常

[查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.4.1)

## etp v0.4.0

### ✨新特性
- 新增springboot 2/3/4版本集成etp的stater，支持嵌入使用，无需安装etp客户端
- 客户端支持自定义连接超时配置
- 支持端口池，可以在etps.toml中指定公网端口的分配范围
- 管理界面展示在线客户端的操作系统信息
- 已认证客户端支持自主上报端口映射信息
- 管理界面体验优化，调整样式布局
### 🐛 问题修复
- 客户端日志文件命名错误修复
- 默认日志收集格式修复
- 代码优化，解决潜在的空指针问题
### ⚙️ 配置变更
- 代理映射的名称可以重复
- 新增端口池配置

  [查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.4.0)

## etp v0.3.2

- 优化服务端项目工程代码结构
- 完善管理界面代码
- 登录有效期改为1天

  [查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.3.2)

## etp v0.3.1
### 🐛 问题修复
- 修复TCP协议无法加载HTTP服务WEB页面问题‼️

[查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.3.1)

## etp v0.3.0
### ✨ 新功能
- 增加SQLite事务管理机制

### 🐛 问题修复
- 修复统一异常处理无法处理所有类型异常问题
- 完善管理面板接口业务逻辑，添加事务支持

[查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.3.0)

## etp v0.2.0
### ✨ 新功能
- 新增易于操作的图形化管理界面。
- 新增支持日志自定义规则配置。
### ⚙️ 配置变更
- 配置文件中的证书配置属性由 `ssl` 更名为 `tls`。
- 端口映射增加status状态字段，用于控制映射的开启状态。

[查看本次发行完整信息 ➡️](https://github.com/xiaoniucode/etp/releases/tag/v0.2.0)
