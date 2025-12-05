#!/bin/bash

# =============================================
# SSL证书自动生成脚本
# =============================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m'

# 默认配置
DEFAULT_ALIAS="serverkey"
DEFAULT_KEYALG="RSA"
DEFAULT_KEYSIZE="2048"
DEFAULT_VALIDITY="365"
DEFAULT_DNAME="CN=localhost"
DEFAULT_CERT_DIR="cert"

# 固定文件名
KEYSTORE_FILE="server.p12"
TRUSTSTORE_FILE="client.p12"
SERVER_CONFIG_FILE="etps.toml"
CLIENT_CONFIG_FILE="etpc.toml"
PASSWORD_BACKUP_FILE="cert-backup.txt"

# 使用说明
usage() {
    echo -e "${GREEN}SSL证书自动生成脚本${NC}"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo -e "${BLUE}选项:${NC}"
    echo "  -alias <名称>             密钥别名 (默认: ${DEFAULT_ALIAS})"
    echo "  -dname <主题>             证书主题DN (默认: ${DEFAULT_DNAME})"
    echo "  -keyalg <算法>            密钥算法 (默认: ${DEFAULT_KEYALG})"
    echo "  -keysize <大小>           密钥长度 (默认: ${DEFAULT_KEYSIZE})"
    echo "  -validity <天数>          有效期 (默认: ${DEFAULT_VALIDITY})"
    echo "  -serverStorePass <密码>   服务器密钥库密码 (默认: 随机生成)"
    echo "  -clientStorePass <密码>   客户端信任库密码 (默认: 随机生成)"
    echo "  -keypass <密码>           私钥密码 (默认: 与serverStorePass相同)"
    echo "  -d, --dir <目录>          输出目录 (默认: ${DEFAULT_CERT_DIR})"
    echo "  -h, --help               显示此帮助信息"
    echo ""
    echo -e "${YELLOW}示例:${NC}"
    echo "  $0"
    echo "  $0 -alias myserver -dname \"CN=myserver.com\""
    echo "  $0 -serverStorePass s123456 -clientStorePass c123456 -keypass k123456"
    echo "  $0 --dir etpcert"
    echo "  $0 --dir ssl-config"
}

# 生成随机密码
generate_random_password() {
    openssl rand -base64 16 | tr -d '/+=' | cut -c1-12
}

# 解析命令行参数
ALIAS="$DEFAULT_ALIAS"
KEYALG="$DEFAULT_KEYALG"
KEYSIZE="$DEFAULT_KEYSIZE"
VALIDITY="$DEFAULT_VALIDITY"
DNAME="$DEFAULT_DNAME"
SERVER_STOREPASS=""
CLIENT_STOREPASS=""
KEYPASS=""
CERT_DIR="$DEFAULT_CERT_DIR"

while [[ $# -gt 0 ]]; do
    case $1 in
        -alias)
            ALIAS="$2"
            shift 2
            ;;
        -keyalg)
            KEYALG="$2"
            shift 2
            ;;
        -keysize)
            KEYSIZE="$2"
            shift 2
            ;;
        -validity)
            VALIDITY="$2"
            shift 2
            ;;
        -dname)
            DNAME="$2"
            shift 2
            ;;
        -serverStorePass)
            SERVER_STOREPASS="$2"
            shift 2
            ;;
        -clientStorePass)
            CLIENT_STOREPASS="$2"
            shift 2
            ;;
        -keypass)
            KEYPASS="$2"
            shift 2
            ;;
        -d|--dir)
            CERT_DIR="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}错误: 未知参数 $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# 生成随机密码（如果未指定）
if [[ -z "$SERVER_STOREPASS" ]]; then
    SERVER_STOREPASS=$(generate_random_password)
    echo -e "${YELLOW}生成随机serverStorePass: $SERVER_STOREPASS${NC}"
fi

if [[ -z "$CLIENT_STOREPASS" ]]; then
    CLIENT_STOREPASS=$(generate_random_password)
    echo -e "${YELLOW}生成随机clientStorePass: $CLIENT_STOREPASS${NC}"
fi

# 如果未指定keypass，使用与serverStorePass相同的密码
if [[ -z "$KEYPASS" ]]; then
    KEYPASS="$SERVER_STOREPASS"
    echo -e "${YELLOW}使用serverStorePass作为keypass${NC}"
fi

# 显示配置信息
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}           SSL证书生成配置${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${BLUE}输出目录:${NC}          $CERT_DIR/"
echo -e "${BLUE}密钥别名:${NC}          $ALIAS"
echo -e "${BLUE}证书主题:${NC}          $DNAME"
echo -e "${BLUE}密钥算法:${NC}          $KEYALG - $KEYSIZE 位"
echo -e "${BLUE}有效期:${NC}            $VALIDITY 天"
echo -e "${BLUE}固定文件名:${NC}        server.p12, client.p12, etps.toml, etpc.toml"
echo -e "${PURPLE}服务器密钥库密码:${NC}  $SERVER_STOREPASS"
echo -e "${CYAN}客户端信任库密码:${NC}  $CLIENT_STOREPASS"
echo -e "${YELLOW}私钥密码:${NC}          $KEYPASS"
echo -e "${GREEN}=========================================${NC}"
echo ""

# 确认继续
read -p "是否继续生成证书? (y/N): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "已取消操作"
    exit 0
fi

# 创建证书目录
echo -e "${YELLOW}创建输出目录...${NC}"
mkdir -p "$CERT_DIR"

# 步骤1: 生成服务器密钥库
echo -e "${YELLOW}[1/3] 生成服务器密钥库...${NC}"
keytool -genkeypair \
    -alias "$ALIAS" \
    -keyalg "$KEYALG" \
    -keysize "$KEYSIZE" \
    -storetype PKCS12 \
    -keystore "$CERT_DIR/$KEYSTORE_FILE" \
    -dname "$DNAME" \
    -keypass "$KEYPASS" \
    -storepass "$SERVER_STOREPASS" \
    -validity "$VALIDITY"

# 步骤2: 导出服务器证书
echo -e "${YELLOW}[2/3] 导出服务器证书...${NC}"
keytool -exportcert \
    -alias "$ALIAS" \
    -keystore "$CERT_DIR/$KEYSTORE_FILE" \
    -storepass "$SERVER_STOREPASS" \
    -file "$CERT_DIR/server.cer"

# 步骤3: 创建客户端信任库（使用不同的密码）
echo -e "${YELLOW}[3/3] 创建客户端信任库...${NC}"
keytool -importcert \
    -alias "${ALIAS}cert" \
    -file "$CERT_DIR/server.cer" \
    -keystore "$CERT_DIR/$TRUSTSTORE_FILE" \
    -storepass "$CLIENT_STOREPASS" \
    -storetype PKCS12 \
    -noprompt

# 清理中间文件
echo -e "${YELLOW}清理中间文件...${NC}"
rm -f "$CERT_DIR/server.cer"
echo -e "${GREEN}已清理中间文件: $CERT_DIR/server.cer${NC}"

# 验证生成的文件
echo -e "${YELLOW}验证生成的文件...${NC}"
echo -e "${PURPLE}服务器密钥库信息:${NC}"
keytool -list -v -keystore "$CERT_DIR/$KEYSTORE_FILE" -storepass "$SERVER_STOREPASS" | grep -E "别名|创建日期|有效期" | head -3

echo -e "${CYAN}客户端信任库信息:${NC}"
keytool -list -v -keystore "$CERT_DIR/$TRUSTSTORE_FILE" -storepass "$CLIENT_STOREPASS" | grep -E "别名|证书数量" | head -2

# 生成服务端配置文件 etps.toml
echo -e "${YELLOW}生成服务端配置文件 $SERVER_CONFIG_FILE ...${NC}"
cat > "$CERT_DIR/$SERVER_CONFIG_FILE" << EOF
tls = true
[keystore]
path = "$KEYSTORE_FILE"
keyPass = "$KEYPASS"
storePass = "$SERVER_STOREPASS"
EOF

# 生成客户端配置文件 etpc.toml
echo -e "${YELLOW}生成客户端配置文件 $CLIENT_CONFIG_FILE ...${NC}"
cat > "$CERT_DIR/$CLIENT_CONFIG_FILE" << EOF
tls = true
[truststore]
path = "$TRUSTSTORE_FILE"
storePass = "$CLIENT_STOREPASS"
EOF

# 生成密码备份文件 cert-backup.txt
echo -e "${YELLOW}生成密码备份文件 $PASSWORD_BACKUP_FILE ...${NC}"
cat > "$CERT_DIR/$PASSWORD_BACKUP_FILE" << EOF
# =============================================
# SSL证书密码信息
# 生成时间: $(date)
# 请妥善保管此文件！
# =============================================

文件信息:
  服务器密钥库: $KEYSTORE_FILE
  客户端信任库: $TRUSTSTORE_FILE
  服务端配置: $SERVER_CONFIG_FILE
  客户端配置: $CLIENT_CONFIG_FILE

密码信息:
  密钥别名: $ALIAS
  服务器密钥库密码 (serverStorePass): $SERVER_STOREPASS
  客户端信任库密码 (clientStorePass): $CLIENT_STOREPASS
  私钥密码 (keypass): $KEYPASS

其他信息:
  证书有效期: $VALIDITY 天
  证书主题: $DNAME
  密钥算法: $KEYALG - $KEYSIZE 位

EOF

# 显示完成信息
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}          SSL证书生成完成!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${BLUE}所有文件已生成在目录:${NC}"
echo -e "  📁 ${GREEN}$CERT_DIR/${NC}"
echo -e "    ├── ${PURPLE}$KEYSTORE_FILE${NC}           (服务器密钥库)"
echo -e "    ├── ${CYAN}$TRUSTSTORE_FILE${NC}           (客户端信任库)"
echo -e "    ├── ${PURPLE}$SERVER_CONFIG_FILE${NC}             (服务端SSL配置)"
echo -e "    ├── ${CYAN}$CLIENT_CONFIG_FILE${NC}             (客户端SSL配置)"
echo -e "    └── ${YELLOW}$PASSWORD_BACKUP_FILE${NC}        (密码备份)"
echo ""
echo -e "${BLUE}三个密码说明:${NC}"
echo -e "  ${PURPLE}🔐 serverStorePass:${NC} $SERVER_STOREPASS (服务端密钥库)"
echo -e "  ${CYAN}🔐 clientStorePass:${NC} $CLIENT_STOREPASS (客户端信任库)"
echo -e "  ${YELLOW}🔑 keypass:${NC}         $KEYPASS (服务端私钥)"
echo ""
echo -e "${YELLOW}使用说明:${NC}"
echo -e "  1. ${PURPLE}服务端${NC}: 使用 $CERT_DIR/$SERVER_CONFIG_FILE 配置文件"
echo -e "  2. ${CYAN}客户端${NC}: 使用 $CERT_DIR/$CLIENT_CONFIG_FILE 配置文件"
echo -e "  3. 所有文件使用固定名称，路径已正确配置"
echo -e "${GREEN}=========================================${NC}"

# 显示配置文件内容
echo -e "${YELLOW}配置文件内容预览:${NC}"
echo -e "${PURPLE}=== $CERT_DIR/$SERVER_CONFIG_FILE ===${NC}"
cat "$CERT_DIR/$SERVER_CONFIG_FILE"
echo ""
echo -e "${CYAN}=== $CERT_DIR/$CLIENT_CONFIG_FILE ===${NC}"
cat "$CERT_DIR/$CLIENT_CONFIG_FILE"
echo ""

# 安全提醒
echo -e "${RED}安全提醒:${NC}"
echo -e "  • 立即备份并安全删除 $CERT_DIR/$PASSWORD_BACKUP_FILE"
echo -e "  • 设置证书文件权限: chmod 600 $CERT_DIR/*.p12"
echo -e "  • 配置文件包含密码，请妥善保管整个 $CERT_DIR 目录"
echo -e "${GREEN}=========================================${NC}"
