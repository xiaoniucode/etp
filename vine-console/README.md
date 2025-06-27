## 连接到代理服务器

```shell
vtc -H localhost -P 9871 -u root -p 123456
```
![img.png](doc/img.png)

## 命令
客户端管理命令：
```shell
admin $ client -h
Usage: client [-hV] [COMMAND]
客户端管理命令
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  list    查看所有客户端列表
  delete  删除指定客户端
  update  更新客户端
  add     添加客户端
```
代理规则管理命令：
```shell
admin $ proxy -h
Usage: proxy [-hV] [COMMAND]
代理管理命令
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  list    查看所有代理列表
  add     添加一个代理
  delete  删除指定代理
  update  更新代理信息
```
### 查看所有的客户端列表

```shell
client list
```
```shell
admin $ client list
Name  SecretKey                         Status  
------------------------------------------------
客户端1  4b0063baa5ae47c2910fc25265aae4b9  在线      
客户端2  7c0084cbb6bf58d3021fd36376bbf5c0  在线 
```

### 删除某一个客户端

```shell
client delete <secretKey>
```

### 修改客户端信息

```shell
client update <secretKey> --name MySQL
```

### 添加一个客户端
```shell
client add --name user1  
```

### 查看所有代理列表

```shell
proxy list
```

### 添加一个代理

```shell
proxy add  7c0084cbb6bf58d3021fd36376bbf5c0 --name mysql --type tcp --localIP 127.0.0.1 --localPort 3306 --remotePort 3307
```
or

```shell
proxy add 7c0084cbb6bf58d3021fd36376bbf5c0  --type tcp  --localPort 3306  --remotePort 3307
```
### 删除一个代理

```shell
proxy delete 6380
```

### 更新一个代理

```shell
proxy update 7c0084cbb6bf58d3021fd36376bbf5c0  <remotePort> --name mysql --type tcp --localIP 127.0.0.1 --localPort 3306 --remotePort 3307
```
