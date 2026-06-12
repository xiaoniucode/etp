#
# /*
#  *    Copyright 2026 xiaoniucode
#  *
#  *    Licensed under the Apache License, Version 2.0 (the "License");
#  *    you may not use this file except in compliance with the License.
#  *    You may obtain a copy of the License at
#  *
#  *        http://www.apache.org/licenses/LICENSE-2.0
#  *
#  *    Unless required by applicable law or agreed to in writing, software
#  *    distributed under the License is distributed on an "AS IS" BASIS,
#  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  *    See the License for the specific language governing permissions and
#  *    limitations under the License.
#  */
#

#!/bin/bash

set -e

# Linux 使用 /opt/etps
# macOS 使用 ~/.etps（避免 Docker Desktop 挂载权限问题）
if [ "$(uname -s)" = "Darwin" ]; then
    ETPS_HOME="$HOME/.etps"
else
    ETPS_HOME="/opt/etps"
fi

mkdir -p "$ETPS_HOME"/{config,certs,logs,mysql/data}

docker network create etps-net >/dev/null 2>&1 || true

docker run -d \
  --name etps-mysql \
  --network etps-net \
  --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=etps.123456 \
  -e MYSQL_DATABASE=etps \
  -v "$ETPS_HOME/mysql/data:/var/lib/mysql" \
  mysql:8.4

docker run -d \
  --name etps \
  --network etps-net \
  --restart unless-stopped \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 8000-9000:8000-9000 \
  -e MYSQL_HOST=etps-mysql \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=etps \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=etps.123456 \
  -e JAVA_OPTS="-Xms512m -Xmx512m -XX:MaxDirectMemorySize=1024m -XX:+UseG1GC" \
  -v "$ETPS_HOME/config:/app/config" \
  -v "$ETPS_HOME/certs:/app/certs" \
  -v "$ETPS_HOME/logs:/app/logs" \
  xiaoniucode/etps:latest

echo ""
echo "=================================================="
echo "               ETPS 部署完成"
echo "=================================================="
echo ""
echo "【访问信息】"
echo "  管理地址     : http://<服务器IP>:8020"
echo "  Web 管理端口 : 8020"
echo "  控制端口     : 9527"
echo "  映射端口范围 : 8000-9000"
echo ""
echo "【数据库信息】"
echo "  数据库类型   : MySQL 8.4"
echo "  地址         : etps-mysql"
echo "  端口         : 3306"
echo "  数据库名     : etps"
echo "  用户名       : root"
echo "  密码         : etps.123456"
echo ""
echo "【数据目录】"
echo "  MySQL 数据   : $ETPS_HOME/mysql/data"
echo "  配置文件     : $ETPS_HOME/config"
echo "  证书目录     : $ETPS_HOME/certs"
echo "  日志目录     : $ETPS_HOME/logs"
echo ""
echo "【容器名称】"
echo "  ETPS         : etps"
echo "  MySQL        : etps-mysql"
echo "=================================================="