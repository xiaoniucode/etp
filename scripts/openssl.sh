#!/bin/bash

# 创建目录
mkdir -p cert/openssl

# 生成CA
openssl genrsa -out cert/openssl/ca.key 2048
openssl req -x509 -new -nodes -key cert/openssl/ca.key -subj "/CN=ca" -days 3650 -out cert/openssl/ca.crt

# 生成服务端证书
openssl genrsa -out cert/openssl/server.key 2048
openssl req -new -key cert/openssl/server.key -subj "/CN=localhost" -out cert/openssl/server.csr
openssl x509 -req -days 3650 -in cert/openssl/server.csr -CA cert/openssl/ca.crt -CAkey cert/openssl/ca.key -CAcreateserial -out cert/openssl/server.crt -extfile <(echo "subjectAltName=DNS:localhost,IP:127.0.0.1")

# 生成客户端证书
openssl genrsa -out cert/openssl/client.key 2048
openssl req -new -key cert/openssl/client.key -subj "/CN=client" -out cert/openssl/client.csr
openssl x509 -req -days 3650 -in cert/openssl/client.csr -CA cert/openssl/ca.crt -CAkey cert/openssl/ca.key -CAcreateserial -out cert/openssl/client.crt

# 清理
rm -f cert/openssl/*.csr cert/openssl/*.srl

echo "success"