import React, {useEffect, useState} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useBaseUrl from '@docusaurus/useBaseUrl';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';

const GITHUB = 'https://github.com/xiaoniucode/etp';
const GITHUB_ISSUES = 'https://github.com/xiaoniucode/etp/issues';
const GITHUB_DISCUSSIONS = 'https://github.com/xiaoniucode/etp/discussions';

const heroBadges = ['TCP / HTTP', 'TLS 1.3', 'Netty', 'Spring Boot', 'Toml / 控制台', '跨平台'];

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

const colTunnel = {
    title: '场景与隧道',
    items: [
        {label: 'TCP 协议隧道', to: '/docs/use-case/TCP协议隧道'},
        {label: 'HTTP 协议隧道', to: '/docs/use-case/HTTP协议隧道'},
        {label: '隧道加密', to: '/docs/use-case/隧道加密'},
        {label: '客户端说明', to: '/docs/use-case/客户端'},
    ],
};

const colPlatform = {
    title: '平台与集成',
    items: [
        {label: '项目概述', to: '/docs/overview'},
        {label: '安装（Docker 等）', to: '/docs/install/docker'},
        {label: 'Spring 集成', to: '/docs/spring'},
        {label: 'Java SDK', to: '/docs/client-sdk'},
    ],
};

const resourceLinks = [
    {label: '性能测试', to: '/docs/性能测试'},
    {label: '技术清单', to: '/docs/design/技术清单'},
    {label: '常见问题', to: '/docs/faq/faq'},
    {label: '下载', to: '/docs/download'},
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
        hint: '适合已有 Docker 的环境。默认映射控制台 8020、隧道通信 9527；更多端口与数据卷见完整文档。',
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
        hint: '从 GitHub Release 获取 etps.jar，与 etps.toml 放在同一目录（需 JDK 8+）。',
        code: '$ java -jar etps.jar -c etps.toml',
        doc: '/docs/install/linux',
    },
    {
        id: 'binary',
        label: '二进制',
        hint: 'Linux / macOS / Windows 均有对应可执行文件；以下为 Linux / macOS 常见用法，Windows 见安装文档中的 .exe 示例。',
        code: `$ chmod +x etps
$ ./etps -c etps.toml`,
        doc: '/docs/install/linux',
    },
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
        hint: '将 etpc 能力嵌入 Spring Boot，无需单独维护客户端进程；按需选用 Boot 2.x / 3.x 坐标。',
        code: `<dependency>
  <groupId>io.github.xiaoniucode</groupId>
  <artifactId>etp-spring-boot3-starter</artifactId>
  <version>0.2.1</version>
</dependency>`,
        doc: '/docs/spring',
    },
];

function QuickInstall() {
    const [role, setRole] = useState<InstallRole>('server');
    const [methodIndex, setMethodIndex] = useState(0);

    const tabs = role === 'server' ? INSTALL_SERVER : INSTALL_CLIENT;

    useEffect(() => {
        setMethodIndex(0);
    }, [role]);

    const safeIndex = Math.min(methodIndex, tabs.length - 1);
    const active = tabs[safeIndex] ?? tabs[0];

    const windowTitle = `${role === 'server' ? 'etps' : 'etpc'} · ${active.label}`;

    return (
        <section className={styles.qiSection} aria-labelledby="home-qi-title">
            <div className={styles.qiInner}>
                <div className={styles.qiCard}>
                    <header className={styles.qiCardHead}>
                        <h2 id="home-qi-title" className={styles.qiTitle}>
                            快速上手
                        </h2>
                        <p className={styles.qiSubtitle}>
                            选择 <strong>etps</strong> / <strong>etpc</strong> 与安装方式，下方为可复制命令或依赖片段。
                        </p>
                    </header>

                    <div className={styles.qiToolbar}>
                        <div className={styles.qiRoleRow} role="tablist" aria-label="安装角色">
                            <button
                                type="button"
                                role="tab"
                                aria-selected={role === 'server'}
                                className={clsx(styles.qiRoleBtn, role === 'server' && styles.qiRoleBtnActive)}
                                onClick={() => setRole('server')}>
                                服务端 etps
                            </button>
                            <button
                                type="button"
                                role="tab"
                                aria-selected={role === 'client'}
                                className={clsx(styles.qiRoleBtn, role === 'client' && styles.qiRoleBtnActive)}
                                onClick={() => setRole('client')}>
                                客户端 etpc
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
    return (
        <div className={styles.heroVisual} aria-hidden>
            <div className={styles.heroVisualBlob}/>
            <div className={styles.heroVisualGrid}/>
            <svg className={styles.heroVisualSvg} viewBox="0 0 400 320" fill="none" xmlns="http://www.w3.org/2000/svg">
                <defs>
                    <linearGradient id="etp-tunnel-grad" x1="0%" y1="50%" x2="100%" y2="50%">
                        <stop offset="0%" stopColor="var(--ifm-color-primary)" stopOpacity="0.35"/>
                        <stop offset="50%" stopColor="var(--ifm-color-primary)" stopOpacity="0.95"/>
                        <stop offset="100%" stopColor="var(--ifm-color-primary-light)" stopOpacity="0.4"/>
                    </linearGradient>
                    <filter id="etp-tunnel-glow" x="-25%" y="-25%" width="150%" height="150%">
                        <feGaussianBlur stdDeviation="5" result="blur"/>
                        <feMerge>
                            <feMergeNode in="blur"/>
                            <feMergeNode in="SourceGraphic"/>
                        </feMerge>
                    </filter>
                </defs>
                <rect
                    x="48"
                    y="136"
                    width="304"
                    height="48"
                    rx="24"
                    stroke="url(#etp-tunnel-grad)"
                    strokeWidth="2"
                    fill="var(--ifm-color-primary)"
                    opacity="0.08"
                />
                <circle cx="88" cy="160" r="14" fill="var(--ifm-color-primary)" opacity="0.92" filter="url(#etp-tunnel-glow)"/>
                <circle cx="312" cy="160" r="14" fill="var(--ifm-color-primary-lightest)" opacity="0.88"/>
                <path
                    d="M88 160h176"
                    stroke="url(#etp-tunnel-grad)"
                    strokeWidth="3"
                    strokeLinecap="round"
                    strokeDasharray="8 12"
                    className={styles.heroVisualPulse}
                />
                <text x="200" y="92" textAnchor="middle" className={styles.heroVisualCaption} fill="currentColor">
                    内网 → 隧道 → 公网
                </text>
            </svg>
        </div>
    );
}

function FeatureIcon({id}: {id: (typeof features)[number]['id']}) {
    const common = {className: styles.featureIconSvg, viewBox: '0 0 24 24', width: 22, height: 22, 'aria-hidden': true as const};
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
                    <path d="M12 3v3M12 18v3M4.22 4.22l2.12 2.12M17.66 17.66l2.12 2.12M3 12h3M18 12h3M4.22 19.78l2.12-2.12M17.66 6.34l2.12-2.12"/>
                    <circle cx="12" cy="12" r="4"/>
                </svg>
            );
        case 'globe':
            return (
                <svg {...common} fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
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
                                面向内网穿透的隧道代理：TCP / HTTP、TLS 1.3、Netty 全链路高性能传输；可选 Spring Boot
                                集成与可视化控制台，用一份配置把本地服务安全暴露到公网。
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
                                    <svg className={styles.githubIcon} viewBox="0 0 24 24" width="18" height="18" aria-hidden>
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

                <div className={styles.colorRibbon} aria-hidden/>

                <div className={styles.badgeStrip}>
                    <div className={styles.badgeStripInner}>
                        {heroBadges.map((b) => (
                            <span key={b} className={styles.badgePill}>
                                {b}
                            </span>
                        ))}
                    </div>
                </div>

                <QuickInstall/>

                <section className={styles.featureSection} aria-labelledby="home-features-title">
                    <div className={styles.featureSectionInner}>
                        <header className={styles.featureIntro}>
                            <span className={styles.featureIntroMark} aria-hidden/>
                            <div>
                                <h2 id="home-features-title" className={styles.featureIntroTitle}>
                                    核心能力
                                </h2>
                                <p className={styles.featureIntroLead}>
                                    传输、安全、控制台与运维能力分层呈现，便于对照你的部署形态。
                                </p>
                            </div>
                        </header>
                        <ul className={styles.featureGrid}>
                            {features.map((f, i) => (
                                <li key={f.title} className={clsx(styles.featureCard, styles[`featureTone${(i % 3) + 1}`])}>
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

                <section className={styles.splitSection} aria-label="文档导航">
                    <div className={styles.splitSectionBg} aria-hidden/>
                    <div className={styles.splitInner}>
                        <div className={clsx(styles.splitCard, styles.splitCardA)}>
                            <h2 className={styles.splitTitle}>{colTunnel.title}</h2>
                            <ul className={styles.splitList}>
                                {colTunnel.items.map((item) => (
                                    <li key={item.to}>
                                        <Link className={styles.splitLink} to={item.to}>
                                            <span className={styles.splitDot}/>
                                            {item.label}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </div>
                        <div className={clsx(styles.splitCard, styles.splitCardB)}>
                            <h2 className={styles.splitTitle}>{colPlatform.title}</h2>
                            <ul className={styles.splitList}>
                                {colPlatform.items.map((item) => (
                                    <li key={item.to}>
                                        <Link className={styles.splitLink} to={item.to}>
                                            <span className={styles.splitDot}/>
                                            {item.label}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>
                </section>

                <section className={styles.resourceSection} aria-label="扩展阅读">
                    <div className={styles.resourceInner}>
                        {resourceLinks.map((r) => (
                            <Link key={r.to} className={styles.resourcePill} to={r.to}>
                                {r.label}
                            </Link>
                        ))}
                    </div>
                </section>

                <footer className={styles.engage} aria-label="开源与反馈">
                    <div className={styles.engageInner}>
                        <p className={styles.engageLabel}>开源与反馈</p>
                        <nav className={styles.engageNav}>
                            <Link className={styles.engageLink} href={GITHUB}>
                                源码仓库
                            </Link>
                            <span className={styles.engageSep} aria-hidden>
                                ·
                            </span>
                            <Link className={styles.engageLink} href={GITHUB_ISSUES}>
                                Issues
                            </Link>
                            <span className={styles.engageSep} aria-hidden>
                                ·
                            </span>
                            <Link className={styles.engageLink} href={GITHUB_DISCUSSIONS}>
                                Discussions
                            </Link>
                        </nav>
                    </div>
                </footer>
            </div>
        </Layout>
    );
}
