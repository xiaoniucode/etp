import React, {useEffect, useState} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';

const GITHUB = 'https://github.com/xiaoniucode/etp';
const features: Array<{
    id: 'panel' | 'speed' | 'shield' | 'sparkle' | 'globe' | 'stack';
    title: string;
    description: string;
}> = [
    {id: 'panel', title: '管理面板', description: 'Web 控制台简化配置，实时流量观测与统计。'},
    {id: 'speed', title: '高性能传输', description: '全链路零拷贝、压缩与高效协议，可选 GraalVM 毫秒级启动。'},
    {id: 'shield', title: '协议与加密', description: 'TCP/HTTP 及上层协议代理，PKCS#12 与 TLS 1.3。'},
    {id: 'sparkle', title: '简单易用', description: '零外部依赖，静态 Toml 或控制台动态配置。'},
    {id: 'globe', title: '跨平台', description: 'Linux / Windows / macOS / Docker，ARM64 与 AMD64。'},
    {id: 'stack', title: '能力矩阵', description: '端口池、多客户端、无客户端模式、强制下线等。'},
];

type InstallRole = 'server' | 'client';

type InstallTabConfig = {
    id: string;
    label: string;
    hint: string;
    code: string;
    doc: string;
};

const INSTALL_SERVER: InstallTabConfig[] = [
    {
        id: 'docker',
        label: 'Docker',
        hint: '默认登录用户名和密码为 admin / 123456',
        code: `$ docker run -d \\
  --name etps \\
  -p 8020:8020 \\
  -p 9527:9527 \\
  xiaoniucode/etps:latest`,
        doc: '/docs/install/docker',
    },
    {
        id: 'jar',
        label: 'JAR',
        hint: '要求JDK 25+',
        code: '$ java -jar etps.jar -c etps.toml',
        doc: '/docs/install/linux',
    }
];

const INSTALL_CLIENT: InstallTabConfig[] = [
    {
        id: 'binary',
        label: '二进制',
        hint: '与 etpc.toml 同目录运行；跨平台下载对应架构可执行文件，Windows 使用 etpc.exe。',
        code: `$ chmod +x etpc
$ ./etpc -c etpc.toml`,
        doc: '/docs/install/linux',
    },
    {
        id: 'jar',
        label: 'JAR',
        hint: '适合已有 JVM、希望与脚本或 systemd 集成的部署方式。',
        code: '$ java -jar etpc.jar -c etpc.toml',
        doc: '/docs/install/linux',
    },
    {
        id: 'spring',
        label: 'Spring Boot',
        hint: '将 etpc 能力嵌入 Spring Boot，无需单独维护客户端进程',
        code: `<dependency>
  <groupId>io.github.xiaoniucode</groupId>
  <artifactId>etp-spring-boot-starter</artifactId>
  <version>0.3.0</version>
</dependency>`,
        doc: '/docs/spring',
    },
];

function QuickInstall() {
    const [role, setRole] = useState<InstallRole>('server');
    const [methodIndex, setMethodIndex] = useState(0);
    const [copied, setCopied] = useState(false);

    const tabs = role === 'server' ? INSTALL_SERVER : INSTALL_CLIENT;

    useEffect(() => {
        setMethodIndex(0);
    }, [role]);

    useEffect(() => {
        if (copied) {
            const timer = setTimeout(() => setCopied(false), 2000);
            return () => clearTimeout(timer);
        }
    }, [copied]);

    const safeIndex = Math.min(methodIndex, tabs.length - 1);
    const active = tabs[safeIndex] ?? tabs[0];

    const windowTitle = `${role === 'server' ? 'etps' : 'etpc'} · ${active.label}`;

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(active.code);
            setCopied(true);
        } catch (err) {
            console.error('Failed to copy:', err);
        }
    };

    return (
        <section className={styles.qiSection} aria-labelledby="home-qi-title">
            <div className={styles.qiInner}>
                <div className={styles.qiCard}>
                    <div className={styles.qiToolbar}>
                        <div className={styles.qiRoleRow} role="tablist" aria-label="安装角色">
                            <button
                                type="button"
                                role="tab"
                                aria-selected={role === 'server'}
                                className={clsx(styles.qiRoleBtn, role === 'server' && styles.qiRoleBtnActive)}
                                onClick={() => setRole('server')}>
                                服务端
                            </button>
                            <button
                                type="button"
                                role="tab"
                                aria-selected={role === 'client'}
                                className={clsx(styles.qiRoleBtn, role === 'client' && styles.qiRoleBtnActive)}
                                onClick={() => setRole('client')}>
                                客户端
                            </button>
                        </div>
                        <div className={styles.qiTabs} role="tablist" aria-label="安装方式">
                            {tabs.map((t, i) => (
                                <button
                                    key={t.id}
                                    type="button"
                                    role="tab"
                                    aria-selected={i === safeIndex}
                                    className={clsx(styles.qiTab, i === safeIndex && styles.qiTabActive)}
                                    onClick={() => setMethodIndex(i)}>
                                    {t.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className={styles.qiWindow}>
                        <div className={styles.qiWindowBar}>
                            <span className={styles.qiWindowDots} aria-hidden>
                                <span className={styles.qiDotR}/>
                                <span className={styles.qiDotY}/>
                                <span className={styles.qiDotG}/>
                            </span>
                            <span className={styles.qiWindowTitle}>{windowTitle}</span>
                            <button
                                type="button"
                                className={clsx(styles.qiCopyBtn, copied && styles.qiCopyBtnSuccess)}
                                onClick={handleCopy}
                                aria-label={copied ? '已复制' : '复制代码'}
                                title={copied ? '已复制' : '复制代码'}>
                                {copied ? (
                                    <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2">
                                        <path d="M4 10l4 4 8-8" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                ) : (
                                    <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.8">
                                        <rect x="7" y="7" width="9" height="9" rx="1.5"/>
                                        <path
                                            d="M13 7V5a1.5 1.5 0 0 0-1.5-1.5h-6A1.5 1.5 0 0 0 3 5v6c0 .8.7 1.5 1.5 1.5H5"
                                            strokeLinecap="round"/>
                                    </svg>
                                )}
                            </button>
                        </div>
                        <div className={styles.qiWindowMeta}>
                            <p className={styles.qiHint}>{active.hint}</p>
                        </div>
                        <pre className={styles.qiPre}>
                            <code>{active.code}</code>
                        </pre>
                    </div>

                    <footer className={styles.qiFooter}>
                        <Link className={styles.qiDocLink} to={active.doc}>
                            查看完整安装文档 →
                        </Link>
                        <span className={styles.qiFooterSep} aria-hidden>
                            |
                        </span>
                        <Link className={styles.qiAltLink} to="/docs/download">
                            发行包下载
                        </Link>
                    </footer>
                </div>
            </div>
        </section>
    );
}

function HeroVisual() {
    const dashboardUrl = useBaseUrl('img/dashboard.png');
    const dashboardBlackUrl = useBaseUrl('img/dashboard_black.png');
    return (
        <div className={styles.heroVisual}>
            <img
                src={dashboardUrl}
                alt="仪表盘预览"
                className={`${styles.heroVisualImg} ${styles.heroVisualImgLight}`}
                loading="lazy"
            />
            <img
                src={dashboardBlackUrl}
                alt="仪表盘预览"
                className={`${styles.heroVisualImg} ${styles.heroVisualImgDark}`}
                loading="lazy"
            />
        </div>
    );
}

function FeatureIcon({id}: { id: (typeof features)[number]['id'] }) {
    const common = {
        className: styles.featureIconSvg,
        viewBox: '0 0 24 24',
        width: 22,
        height: 22,
        'aria-hidden': true as const
    };
    switch (id) {
        case 'panel':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="3" width="18" height="14" rx="2"/>
                    <path d="M3 9h18M8 17h8" strokeLinecap="round"/>
                </svg>
            );
        case 'speed':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" strokeLinejoin="round"/>
                </svg>
            );
        case 'shield':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
            );
        case 'sparkle':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <path
                        d="M12 3v3M12 18v3M4.22 4.22l2.12 2.12M17.66 17.66l2.12 2.12M3 12h3M18 12h3M4.22 19.78l2.12-2.12M17.66 6.34l2.12-2.12"/>
                    <circle cx="12" cy="12" r="4"/>
                </svg>
            );
        case 'globe':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path
                        d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                </svg>
            );
        case 'stack':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                </svg>
            );
        default:
            return null;
    }
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    const logoUrl = useBaseUrl('img/logo.png');

    return (
        <Layout title={siteConfig.title} description={siteConfig.tagline}>
            <div className={styles.page}>
                <section className={styles.hero}>
                    <div className={styles.heroMesh}/>
                    <div className={styles.heroInner}>
                        <div className={styles.heroCopy}>
                            <p className={styles.heroEyebrow}>Easy Tunnel Proxy · etp</p>
                            <h1 className={styles.heroTitle}>{siteConfig.tagline}</h1>
                            <p className={styles.heroLead}>
                                面向开发和运维的一站式解决方案。TCP/HTTP多协议代理支持，反向代理、TLS传输加密、压缩、负载均衡、精细限流、访问控制、安全认证、
                                SpringBoot集成、管理面板，具备丰富的场景自定义能力
                            </p>
                            <div className={styles.heroLogoRow}>
                                <img src={logoUrl} alt="" className={styles.heroLogoMark} width={52} height={52}/>
                                <span className={styles.heroLogoCaption}>由 Netty 驱动 · Java 生态友好</span>
                            </div>
                            <div className={styles.heroActions}>
                                <Link className={clsx(styles.btn, styles.btnPrimary)} to="/docs/overview">
                                    阅读文档
                                </Link>
                                <Link className={clsx(styles.btn, styles.btnGhost)} href={GITHUB}>
                                    <svg className={styles.githubIcon} viewBox="0 0 24 24" width="18" height="18"
                                         aria-hidden>
                                        <path
                                            fill="currentColor"
                                            d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                                    </svg>
                                    GitHub
                                </Link>
                            </div>
                        </div>
                        <HeroVisual/>
                    </div>
                </section>


                <section className={styles.featureSection} aria-labelledby="home-features-title">
                    <div className={styles.featureSectionInner}>
                        <ul className={styles.featureGrid}>
                            {features.map((f, i) => (
                                <li key={f.title}
                                    className={clsx(styles.featureCard, styles[`featureTone${(i % 3) + 1}`])}>
                                    <div className={styles.featureCardTop}/>
                                    <div className={styles.featureIconWrap}>
                                        <FeatureIcon id={f.id}/>
                                    </div>
                                    <h3 className={styles.featureCardTitle}>{f.title}</h3>
                                    <p className={styles.featureCardDesc}>{f.description}</p>
                                </li>
                            ))}
                        </ul>
                    </div>
                </section>

                <QuickInstall/>
            </div>
        </Layout>
    );
}
