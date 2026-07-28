use std::io::Result;
use std::path::PathBuf;

fn main() -> Result<()> {
    let proto = PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("proto/message.proto");
    let include = PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("proto");
    println!("cargo:rerun-if-changed={}", proto.display());

    let mut config = prost_build::Config::new();
    config.protoc_executable(protoc_bin_vendored::protoc_bin_path().map_err(|e| {
        std::io::Error::new(std::io::ErrorKind::Other, e.to_string())
    })?);
    config.compile_protos(&[proto], &[include])?;
    Ok(())
}
