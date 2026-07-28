#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
mkdir -p "$TMP/src" "$TMP/out"

cat >"$TMP/Cargo.toml" <<'EOF'
[package]
name = "gen"
version = "0.0.0"
edition = "2021"
[dependencies]
prost-build = "0.13"
protoc-bin-vendored = "3"
EOF

cat >"$TMP/src/main.rs" <<'EOF'
fn main() {
    let out = std::env::var("OUT").unwrap();
    std::env::set_var("OUT_DIR", &out);
    std::env::set_var("PROTOC", protoc_bin_vendored::protoc_bin_path().unwrap());
    prost_build::Config::new()
        .compile_protos(&[std::env::var("PROTO").unwrap()], &[std::env::var("INC").unwrap()])
        .unwrap();
}
EOF

PROTO="$(pwd)/proto/message.proto" INC="$(pwd)/proto" OUT="$TMP/out" \
  cargo run --manifest-path "$TMP/Cargo.toml" -q

mkdir -p src/message
cp "$TMP/out"/io.github.lxien.orbien.core.message.rs src/message/generated.rs
echo "src/message/generated.rs"
