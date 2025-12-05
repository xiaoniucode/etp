#!/bin/bash

# =============================================
# SSL Certificate Auto-Generation Script
# =============================================

set -e  # Exit immediately if a command exits with a non-zero status

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Default configuration
DEFAULT_ALIAS="serverkey"
DEFAULT_KEYALG="RSA"
DEFAULT_KEYSIZE="2048"
DEFAULT_VALIDITY="365"
DEFAULT_DNAME="CN=localhost"
DEFAULT_CERT_DIR="cert"

# Fixed filenames
KEYSTORE_FILE="server.p12"
TRUSTSTORE_FILE="client.p12"
SERVER_CONFIG_FILE="etps.toml"
CLIENT_CONFIG_FILE="etpc.toml"
PASSWORD_BACKUP_FILE="cert-backup.txt"

# Usage instructions
usage() {
    echo -e "${GREEN}SSL Certificate Auto-Generation Script${NC}"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo -e "${BLUE}Options:${NC}"
    echo "  -alias <name>             Key alias (default: ${DEFAULT_ALIAS})"
    echo "  -dname <subject>          Certificate subject DN (default: ${DEFAULT_DNAME})"
    echo "  -keyalg <algorithm>       Key algorithm (default: ${DEFAULT_KEYALG})"
    echo "  -keysize <size>           Key length (default: ${DEFAULT_KEYSIZE})"
    echo "  -validity <days>          Validity period (default: ${DEFAULT_VALIDITY})"
    echo "  -serverStorePass <pass>   Server keystore password (default: randomly generated)"
    echo "  -clientStorePass <pass>   Client truststore password (default: randomly generated)"
    echo "  -keypass <pass>           Private key password (default: same as serverStorePass)"
    echo "  -d, --dir <directory>     Output directory (default: ${DEFAULT_CERT_DIR})"
    echo "  -h, --help                Show this help message"
    echo ""
    echo -e "${YELLOW}Examples:${NC}"
    echo "  $0"
    echo "  $0 -alias myserver -dname \"CN=myserver.com\""
    echo "  $0 -serverStorePass s123456 -clientStorePass c123456 -keypass k123456"
    echo "  $0 --dir etpcert"
    echo "  $0 --dir ssl-config"
}

# Generate random password
generate_random_password() {
    openssl rand -base64 16 | tr -d '/+=' | cut -c1-12
}

# Parse command-line arguments
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
            echo -e "${RED}Error: Unknown parameter $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Generate random passwords (if not specified)
if [[ -z "$SERVER_STOREPASS" ]]; then
    SERVER_STOREPASS=$(generate_random_password)
    echo -e "${YELLOW}Generated random serverStorePass: $SERVER_STOREPASS${NC}"
fi

if [[ -z "$CLIENT_STOREPASS" ]]; then
    CLIENT_STOREPASS=$(generate_random_password)
    echo -e "${YELLOW}Generated random clientStorePass: $CLIENT_STOREPASS${NC}"
fi

# If keypass not specified, use same as serverStorePass
if [[ -z "$KEYPASS" ]]; then
    KEYPASS="$SERVER_STOREPASS"
    echo -e "${YELLOW}Using serverStorePass as keypass${NC}"
fi

# Display configuration information
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}           SSL Certificate Generation Configuration${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${BLUE}Output directory:${NC}          $CERT_DIR/"
echo -e "${BLUE}Key alias:${NC}          $ALIAS"
echo -e "${BLUE}Certificate subject:${NC}          $DNAME"
echo -e "${BLUE}Key algorithm:${NC}          $KEYALG - $KEYSIZE bits"
echo -e "${BLUE}Validity period:${NC}            $VALIDITY days"
echo -e "${BLUE}Fixed filenames:${NC}        server.p12, client.p12, etps.toml, etpc.toml"
echo -e "${PURPLE}Server keystore password:${NC}  $SERVER_STOREPASS"
echo -e "${CYAN}Client truststore password:${NC}  $CLIENT_STOREPASS"
echo -e "${YELLOW}Private key password:${NC}          $KEYPASS"
echo -e "${GREEN}=========================================${NC}"
echo ""

# Confirm continuation
read -p "Continue with certificate generation? (y/N): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "Operation canceled"
    exit 0
fi

# Create certificate directory
echo -e "${YELLOW}Creating output directory...${NC}"
mkdir -p "$CERT_DIR"

# Step 1: Generate server keystore
echo -e "${YELLOW}[1/3] Generating server keystore...${NC}"
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

# Step 2: Export server certificate
echo -e "${YELLOW}[2/3] Exporting server certificate...${NC}"
keytool -exportcert \
    -alias "$ALIAS" \
    -keystore "$CERT_DIR/$KEYSTORE_FILE" \
    -storepass "$SERVER_STOREPASS" \
    -file "$CERT_DIR/server.cer"

# Step 3: Create client truststore (using different password)
echo -e "${YELLOW}[3/3] Creating client truststore...${NC}"
keytool -importcert \
    -alias "${ALIAS}cert" \
    -file "$CERT_DIR/server.cer" \
    -keystore "$CERT_DIR/$TRUSTSTORE_FILE" \
    -storepass "$CLIENT_STOREPASS" \
    -storetype PKCS12 \
    -noprompt

# Clean up intermediate files
echo -e "${YELLOW}Cleaning up intermediate files...${NC}"
rm -f "$CERT_DIR/server.cer"
echo -e "${GREEN}Cleaned up intermediate file: $CERT_DIR/server.cer${NC}"

# Verify generated files
echo -e "${YELLOW}Verifying generated files...${NC}"
echo -e "${PURPLE}Server keystore information:${NC}"
keytool -list -v -keystore "$CERT_DIR/$KEYSTORE_FILE" -storepass "$SERVER_STOREPASS" | grep -E "Alias|Creation date|Validity" | head -3

echo -e "${CYAN}Client truststore information:${NC}"
keytool -list -v -keystore "$CERT_DIR/$TRUSTSTORE_FILE" -storepass "$CLIENT_STOREPASS" | grep -E "Alias|Entry type" | head -2

# Generate server configuration file etps.toml
echo -e "${YELLOW}Generating server configuration file $SERVER_CONFIG_FILE ...${NC}"
cat > "$CERT_DIR/$SERVER_CONFIG_FILE" << EOF
tls = true
[keystore]
path = "$KEYSTORE_FILE"
keyPass = "$KEYPASS"
storePass = "$SERVER_STOREPASS"
EOF

# Generate client configuration file etpc.toml
echo -e "${YELLOW}Generating client configuration file $CLIENT_CONFIG_FILE ...${NC}"
cat > "$CERT_DIR/$CLIENT_CONFIG_FILE" << EOF
tls = true
[truststore]
path = "$TRUSTSTORE_FILE"
storePass = "$CLIENT_STOREPASS"
EOF

# Generate password backup file cert-backup.txt
echo -e "${YELLOW}Generating password backup file $PASSWORD_BACKUP_FILE ...${NC}"
cat > "$CERT_DIR/$PASSWORD_BACKUP_FILE" << EOF
# =============================================
# SSL Certificate Password Information
# Generated at: $(date)
# Please keep this file secure!
# =============================================

File information:
  Server keystore: $KEYSTORE_FILE
  Client truststore: $TRUSTSTORE_FILE
  Server config: $SERVER_CONFIG_FILE
  Client config: $CLIENT_CONFIG_FILE

Password information:
  Key alias: $ALIAS
  Server keystore password (serverStorePass): $SERVER_STOREPASS
  Client truststore password (clientStorePass): $CLIENT_STOREPASS
  Private key password (keypass): $KEYPASS

Other information:
  Certificate validity: $VALIDITY days
  Certificate subject: $DNAME
  Key algorithm: $KEYALG - $KEYSIZE bits

EOF

# Display completion message
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}          SSL Certificate Generation Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${BLUE}All files generated in directory:${NC}"
echo -e "  📁 ${GREEN}$CERT_DIR/${NC}"
echo -e "    ├── ${PURPLE}$KEYSTORE_FILE${NC}           (server keystore)"
echo -e "    ├── ${CYAN}$TRUSTSTORE_FILE${NC}           (client truststore)"
echo -e "    ├── ${PURPLE}$SERVER_CONFIG_FILE${NC}             (server SSL config)"
echo -e "    ├── ${CYAN}$CLIENT_CONFIG_FILE${NC}             (client SSL config)"
echo -e "    └── ${YELLOW}$PASSWORD_BACKUP_FILE${NC}        (password backup)"
echo ""
echo -e "${BLUE}Three passwords explained:${NC}"
echo -e "  ${PURPLE}🔐 serverStorePass:${NC} $SERVER_STOREPASS (server keystore)"
echo -e "  ${CYAN}🔐 clientStorePass:${NC} $CLIENT_STOREPASS (client truststore)"
echo -e "  ${YELLOW}🔑 keypass:${NC}         $KEYPASS (server private key)"
echo ""
echo -e "${YELLOW}Usage instructions:${NC}"
echo -e "  1. ${PURPLE}Server${NC}: Use $CERT_DIR/$SERVER_CONFIG_FILE configuration file"
echo -e "  2. ${CYAN}Client${NC}: Use $CERT_DIR/$CLIENT_CONFIG_FILE configuration file"
echo -e "  3. All files use fixed names, paths are correctly configured"
echo -e "${GREEN}=========================================${NC}"

# Display configuration file contents
echo -e "${YELLOW}Configuration file preview:${NC}"
echo -e "${PURPLE}=== $CERT_DIR/$SERVER_CONFIG_FILE ===${NC}"
cat "$CERT_DIR/$SERVER_CONFIG_FILE"
echo ""
echo -e "${CYAN}=== $CERT_DIR/$CLIENT_CONFIG_FILE ===${NC}"
cat "$CERT_DIR/$CLIENT_CONFIG_FILE"
echo ""

# Security reminder
echo -e "${RED}Security reminder:${NC}"
echo -e "  • Immediately back up and securely delete $CERT_DIR/$PASSWORD_BACKUP_FILE"
echo -e "  • Set certificate file permissions: chmod 600 $CERT_DIR/*.p12"
echo -e "  • Configuration files contain passwords, keep the entire $CERT_DIR directory secure"
echo -e "${GREEN}=========================================${NC}"
