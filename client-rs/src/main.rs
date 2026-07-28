use std::path::PathBuf;
use std::process::ExitCode;

use anyhow::{Context, Result};
use clap::{Parser, Subcommand};
use tokio::sync::mpsc;
use tracing::{error, info};
use tracing_subscriber::EnvFilter;

use orbien_rs::{load_from_path, run_agent};

#[derive(Debug, Parser)]
#[command(
    name = "orbien",
    version,
    about = "Orbien 客户端"
)]
struct Cli {
    #[arg(short = 'c', long = "config")]
    config: Option<PathBuf>,

    #[command(subcommand)]
    command: Option<Commands>,
}

#[derive(Debug, Subcommand)]
enum Commands {
    Run {
        #[arg(value_name = "CONFIG")]
        config: Option<PathBuf>,
    },
}

#[tokio::main]
async fn main() -> ExitCode {
    // rustls 要求在首次使用 TLS 前安装进程级 CryptoProvider
    let _ = rustls::crypto::ring::default_provider().install_default();

    init_tracing();
    if let Err(e) = real_main().await {
        error!("{e:#}");
        return ExitCode::FAILURE;
    }
    ExitCode::SUCCESS
}

async fn real_main() -> Result<()> {
    let cli = Cli::parse();
    let config = match (&cli.config, &cli.command) {
        (Some(path), None) => path.clone(),
        (None, Some(Commands::Run { config })) => config
            .clone()
            .unwrap_or_else(|| PathBuf::from("orbien.toml")),
        (None, None) => {
            Cli::parse_from(["orbien", "--help"]);
            unreachable!();
        }
        (Some(_), Some(_)) => {
            anyhow::bail!("请使用 `orbien -c <path>` 或 `orbien run <path>`启动");
        }
    };

    start_agent(config).await
}

async fn start_agent(config: PathBuf) -> Result<()> {
    info!("加载配置: {}", config.display());
    let app_config = load_from_path(&config)
        .with_context(|| format!("加载配置失败: {}", config.display()))?;
    info!(
        "目标服务器 {}:{}，代理数量 {}",
        app_config.server_addr,
        app_config.server_port,
        app_config.proxies.iter().filter(|p| p.enabled).count()
    );

    let (shutdown_tx, shutdown_rx) = mpsc::channel(1);
    tokio::spawn(async move {
        wait_shutdown_signal().await;
        info!("收到停止信号");
        let _ = shutdown_tx.send(()).await;
    });

    run_agent(app_config, shutdown_rx).await
}

async fn wait_shutdown_signal() {
    let ctrl_c = async {
        if let Err(e) = tokio::signal::ctrl_c().await {
            error!("监听 Ctrl-C 失败: {e}");
        }
    };

    #[cfg(unix)]
    let terminate = async {
        match tokio::signal::unix::signal(tokio::signal::unix::SignalKind::terminate()) {
            Ok(mut sig) => {
                sig.recv().await;
            }
            Err(e) => error!("监听 SIGTERM 失败: {e}"),
        }
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {}
        _ = terminate => {}
    }
}

fn init_tracing() {
    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info"));
    tracing_subscriber::fmt()
        .with_env_filter(filter)
        .with_target(false)
        .init();
}
