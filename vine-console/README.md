## 连接到代理服务器

```shell
vtc -H localhost -P 9871 -u root -p 123456
```
![img.png](doc/img.png)

## 命令

查看所有的客户端列表

client list

删除某一个客户端

client delete <secretKey>

修改客户端信息

client update <secretKey> --name 小明

添加一个客户端

client add --name any --secretKey 222432


查看所有代理列表

proxy list

添加一个代理

proxy add  <secretKey> --name mysql --type tcp --localIP 127.0.0.1 --localPort 3306 --remotePort 3307

or

proxy add <secretKey>  --type tcp  --localPort 3306  --remotePort 3307

删除一个代理

proxy delete <secretKey>  <remotePort>

更新一个代理

proxy update <secretKey>  <remotePort> --name mysql --type tcp --localIP 127.0.0.1 --localPort 3306 --remotePort 3307
