# 版本历史
详细发布细节请往Github [Release](https://github.com/xiaoniucode/etp/releases) 页面查看

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
