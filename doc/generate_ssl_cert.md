# **TLS 证书自动生成脚本使用手册**  
**—— 适用于内网穿透项目 PKCS12 格式证书一键生成**

---

## **1. 文档概述**

### **1.2 脚本功能**
本脚本用于**一键生成适用于内网穿透项目的双向TLS 证书对**，输出符合 PKCS12 标准的密钥库与信任库，并自动生成服务端与客户端的 TOML 配置文件。

### **1.3 适用场景**
- 内网穿透工具（如 `etp-server` / `etp-client`）
- 任何需要 **服务端证书 + 客户端信任库** 的 TLS 互信场景
- 开发、测试、生产环境快速部署 TLS 证书

### **1.4 输出文件**
| 文件名 | 类型 | 用途                 |
|--------|------|--------------------|
| `server.p12` | PKCS12 密钥库 | 服务端私钥 + 证书         |
| `client.p12` | PKCS12 信任库 | 客户端信任的服务端证书        |
| `etps.toml` | TOML 配置文件 | 服务端 TLS 配置         |
| `etpc.toml` | TOML 配置文件 | 客户端 TLS 配置         |
| `cert-backup.txt` | 明文密码备份 | 密码记录（**需立即加密或删除**） |

---

## **2. 环境要求**

| 项目 | 要求 |
|------|------|
| 操作系统 | Linux / macOS / WSL2 |
| 运行权限 | 可执行权限（`chmod +x generate_ssl_cert.sh`） |
| 依赖工具 | `bash`, `keytool`（JDK 8+）, `openssl` |
| 磁盘空间 | ≥ 10 MB `MB`（含输出目录） |

> **验证依赖是否安装：**
```bash
java -version && keytool -h >/dev/null && openssl version
```

---

## **3. 使用方法**

### **3.1 赋予执行权限**
```bash
chmod +x generate_ssl_cert.sh
```

### **3.2 查看帮助信息**
```bash
./generate_ssl_cert.sh --help
```

---

## **4. 命令行参数详解**

| 参数 | 短选项 | 说明 | 默认值 | 示例 |
|------|--------|------|--------|------|
| `-alias` | — | 密钥别名 | `serverkey` | `-alias myserver` |
| `-dname` | — | 证书主题 DN | `CN= Anslocalhost` | `-dname "CN=proxy.example.com"` |
| `-keyalg` | — | 密钥算法 | `RSA` | `-keyalg EC` |
| `-keysize` | — | 密钥长度（位） | `2048` | `-keysize 4096` |
| `-validity` | — | 证书有效期（天） | `365` | `-validity 730` |
| `-serverStorePass` | — | 服务器密钥库密码 | 随机生成 | `-serverStorePass s3cur3!` |
| `-clientStorePass` | — | 客户端信任库密码 | 随机生成 | `-clientStorePass cl!3nt` |
| `-keypass` | — | 私钥密码 | 等同 `serverStorePass` | `-keypass pr!v4t3` |
| `-d`, `--dir` | `-d` | 输出目录 | `cert` | `--dir ssl-certs` |
| `-h`, `--help` | `-h` | 显示帮助 | — | `--help` |

> **注意**：所有密码支持特殊字符，但建议避免 `'`、`"`、`$` 等 shell 敏感字符。

---

## **5. 使用示例**

### **示例 1：使用全部默认值**
```bash
./generate_ssl_cert.sh
```
> 输出到 `./cert/` 目录，密码自动生成。

---

### **示例 2：自定义域名与别名**
```bash
./generate_ssl_cert.sh \
  -alias tunnel-server \
  -dname "CN=tunnel.mycorp.local" \
  -validity 365
```

---

### **示例 3：指定密码与输出目录**
```bash
./generate_ssl_cert.sh \
  --dir ssl-config \
  -serverStorePass "Server@2025" \
  -clientStorePass "Client@2025" \
  -keypass "Key@2025"
```

---

### **示例 4：高安全性配置（RSA 4096 + 2年有效期）**
```bash
./generate_ssl_cert.sh \
  -keyalg RSA \
  -keysize 4096 \
  -validity 730 \
  -dname "CN=secure-gateway.example.com, O=MyCorp, L=Beijing, C=CN"
```

---

## **6. 生成流程详解**

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1 | 创建输出目录 | `mkdir -p $CERT_DIR` |
| 2 | 生成 `server.p12` | 使用 `keytool -genkeypair` 生成私钥+证书 |
| 3 | 导出证书 `server.cer` | 临时文件，用于导入信任库 |
| 4 | 创建 `client.p12` | 导入 `server.cer` 作为信任证书 |
| 5 | 清理中间文件 | 删除 `server.cer` |
| 6 | 生成 `etps.toml` | 服务端配置 |
| 7 | 生成 `etpc.toml` | 客户端配置 |
| 8 | 生成 `cert-backup.txt` | 密码备份（**敏感文件**） |

---

## **7. 输出文件结构**

```text
ssl-config/                  ← 输出目录（可自定义）
├── server.p12               ← 服务端密钥库（私钥+证书）
├── client.p12               ← 客户端信任库（信任 server 证书）
├── etps.toml                ← 服务端 TOML 配置
├── etpc.toml                ← 客户端 TOML 配置
└── cert-backup.txt          ← 密码明文备份（需立即处理）
```

---

## **8. 配置文件内容说明**

### **8.1 `etps.toml`（服务端）**
```toml
ssl = true
[keystore]
path = "server.p12"
keyPass = "Key@2025"
storePass = "Server@2025"
```

### **8.2 `etpc.toml`（客户端）**
```toml
ssl = true
[truststore]
path = "client.p12"
storePass = "Client@2025"
```

> 路径为**相对路径**，适用于与配置文件同目录部署。

---

## **9. 安全最佳实践**

| 操作 | 命令 | 说明 |
|------|------|------|
| 设置文件权限 | `chmod 600 ssl-config/*.p12` | 仅拥有者可读写 |
| 备份密码文件 | `scp ssl-config/cert-backup.txt secure-vault:/backups/` | 加密传输 |
| 删除明文密码 | `shred -u ssl-config/cert-backup.txt` | 安全删除 |
| 定期轮换证书 | 每 90 天运行一次脚本 | 避免长期使用同一证书 |

---

## **10. 故障排查**

| 问题 | 原因 | 解决方案 |
|------|------|-----------|
| `keytool: command not found` | JDK 未安装或未加入 PATH | 安装 OpenJDK 并配置环境变量 |
| `Permission denied` | 输出目录无写权限 | 使用 `sudo` 或更改目录权限 |
| 密码包含特殊字符导致失败 | shell 解析错误 | 使用单引号包裹：`-'serverStorePass' 'pass$123'` |
| `client.p12` 导入失败 | `server.cer` 未生成 | 检查 `server.p12` 是否成功生成 |
| 客户端无法连接 | 信任库未包含服务端证书 | 确认 `client.p12` 包含别名 `serverkeycert` |

---
