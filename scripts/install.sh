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
mkdir -p /opt/etps/{config,certs,logs,mysql/data}
cd /opt/etps

docker network create etps-net

docker run -d \
  --name etps-mysql \
  --network etps-net \
  --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=etps.123456 \
  -e MYSQL_DATABASE=etps \
  -v /opt/etps/mysql/data:/var/lib/mysql \
  mysql:8.4

docker run -d \
  --name etps \
  --network etps-net \
  --restart unless-stopped \
  -p 8020:8020 \
  -p 9527:9527 \
  -p 1024-65535:1024-65535 \
  -e MYSQL_HOST=etps-mysql \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=etps \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=etps.123456 \
  -e JAVA_OPTS="-Xms512m -Xmx512m -XX:MaxDirectMemorySize=1024m -XX:+UseG1GC" \
  -v /opt/etps/config:/app/config \
  -v /opt/etps/certs:/app/certs \
  -v /opt/etps/logs:/app/logs \
  xiaoniucode/etps:latest

  echo "========================================"
  echo "etps Docker 快速体验"
  echo "========================================"
  echo "MySQL 数据目录 : /opt/etps/mysql/data"
  echo "配置目录       : /opt/etps/config"
  echo "证书目录       : /opt/etps/certs"
  echo "日志目录       : /opt/etps/logs"
  echo "Web管理端口    : 8020"
  echo "控制端口       : 9527"
  echo "映射端口范围   : 1024-65535"
  echo "========================================"