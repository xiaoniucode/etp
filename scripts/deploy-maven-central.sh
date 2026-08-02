#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

rm -rf \
  core/target \
  common/target \
  client/target

mvn clean deploy -pl core,common,client -am

cd "${ROOT_DIR}/orbien-spring-boot-starter"
rm -rf target
mvn clean deploy
