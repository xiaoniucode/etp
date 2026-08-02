use std::fs::File;
use std::io::BufReader;
use std::path::Path;
use std::sync::{Arc, Mutex};

use anyhow::{anyhow, Context, Result};
use rustls::client::danger::{HandshakeSignatureValid, ServerCertVerified, ServerCertVerifier};
use rustls::pki_types::{CertificateDer, ServerName, UnixTime};
use rustls::{ClientConfig, DigitallySignedStruct, Error as TlsError, SignatureScheme};
use rustls_pemfile::{certs, private_key};
use tracing::warn;

use crate::config::TlsConfig;

static TLS_CLIENT_CACHE: Mutex<Option<(String, Arc<ClientConfig>)>> = Mutex::new(None);

pub fn build_client_config(tls: &TlsConfig) -> Result<Arc<ClientConfig>> {
    let key = cache_key(tls);
    if let Ok(guard) = TLS_CLIENT_CACHE.lock() {
        if let Some((cached_key, cfg)) = guard.as_ref() {
            if *cached_key == key {
                return Ok(cfg.clone());
            }
        }
    }

    let built = build_client_config_uncached(tls)?;
    if let Ok(mut guard) = TLS_CLIENT_CACHE.lock() {
        *guard = Some((key, built.clone()));
    }
    Ok(built)
}

fn cache_key(tls: &TlsConfig) -> String {
    format!(
        "{}|{}|{}",
        path_key(&tls.ca_file),
        path_key(&tls.cert_file),
        path_key(&tls.key_file),
    )
}

fn path_key(path: &Option<std::path::PathBuf>) -> String {
    path.as_ref()
        .map(|p| p.display().to_string())
        .unwrap_or_default()
}

fn build_client_config_uncached(tls: &TlsConfig) -> Result<Arc<ClientConfig>> {
    let builder = ClientConfig::builder();

    let builder = if let Some(ca) = &tls.ca_file {
        let roots = load_root_certs(ca)?;
        builder.with_root_certificates(roots)
    } else {
        warn!("未配置 transport.tls.ca_file，将跳过服务端证书校验");
        builder
            .dangerous()
            .with_custom_certificate_verifier(Arc::new(NoVerifier))
    };

    let config = if let (Some(cert), Some(key)) = (&tls.cert_file, &tls.key_file) {
        let certs = load_certs(cert)?;
        let key = load_private_key(key)?;
        builder
            .with_client_auth_cert(certs, key)
            .context("加载客户端证书失败")?
    } else {
        builder.with_no_client_auth()
    };

    Ok(Arc::new(config))
}

fn load_root_certs(path: &Path) -> Result<rustls::RootCertStore> {
    let mut roots = rustls::RootCertStore::empty();
    let file = File::open(path).with_context(|| format!("打开 CA 文件失败: {}", path.display()))?;
    let mut reader = BufReader::new(file);
    let certs: Result<Vec<_>, _> = certs(&mut reader).collect();
    let certs = certs.context("解析 CA 证书失败")?;
    for cert in certs {
        roots
            .add(cert)
            .map_err(|e| anyhow!("添加 CA 证书失败: {e}"))?;
    }
    if roots.is_empty() {
        roots.extend(webpki_roots::TLS_SERVER_ROOTS.iter().cloned());
    }
    Ok(roots)
}

fn load_certs(path: &Path) -> Result<Vec<CertificateDer<'static>>> {
    let file = File::open(path).with_context(|| format!("打开证书失败: {}", path.display()))?;
    let mut reader = BufReader::new(file);
    let certs: Result<Vec<_>, _> = certs(&mut reader).collect();
    certs.context("解析证书失败")
}

fn load_private_key(path: &Path) -> Result<rustls::pki_types::PrivateKeyDer<'static>> {
    let file = File::open(path).with_context(|| format!("打开私钥失败: {}", path.display()))?;
    let mut reader = BufReader::new(file);
    private_key(&mut reader)
        .context("解析私钥失败")?
        .ok_or_else(|| anyhow!("私钥文件中未找到有效密钥"))
}

#[derive(Debug)]
struct NoVerifier;

impl ServerCertVerifier for NoVerifier {
    fn verify_server_cert(
        &self,
        _end_entity: &CertificateDer<'_>,
        _intermediates: &[CertificateDer<'_>],
        _server_name: &ServerName<'_>,
        _ocsp_response: &[u8],
        _now: UnixTime,
    ) -> Result<ServerCertVerified, TlsError> {
        Ok(ServerCertVerified::assertion())
    }

    fn verify_tls12_signature(
        &self,
        _message: &[u8],
        _cert: &CertificateDer<'_>,
        _dss: &DigitallySignedStruct,
    ) -> Result<HandshakeSignatureValid, TlsError> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn verify_tls13_signature(
        &self,
        _message: &[u8],
        _cert: &CertificateDer<'_>,
        _dss: &DigitallySignedStruct,
    ) -> Result<HandshakeSignatureValid, TlsError> {
        Ok(HandshakeSignatureValid::assertion())
    }

    fn supported_verify_schemes(&self) -> Vec<SignatureScheme> {
        rustls::crypto::ring::default_provider()
            .signature_verification_algorithms
            .supported_schemes()
    }
}
