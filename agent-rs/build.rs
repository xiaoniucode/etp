use std::io::Result;
use std::path::PathBuf;

fn main() -> Result<()> {
    let proto = PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("proto/message.proto");
    let include = PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("proto");
    println!("cargo:rerun-if-changed={}", proto.display());

    prost_build::Config::new()
        .compile_protos(&[proto], &[include])?;
    Ok(())
}
