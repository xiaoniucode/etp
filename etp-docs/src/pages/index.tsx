// src/pages/index.tsx
import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';
function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero', styles.heroBanner)}>
            <div className="container">
                <div className={styles.heroLogoWrapper}>
                    <img
                        src="img/logo.png"
                        alt={siteConfig.title}
                        className={styles.heroLogo}
                    />
                </div>
                <p className={styles.heroTagline}>{siteConfig.tagline}</p>
                <p className={styles.heroSubtitle}>
                    简单、高效、支持TCP协议以及支持与SpringBoot&Cloud生态无缝集成的隧道代理解决方案
                </p>
                <div className={styles.buttons}>
                    <Link
                        className={clsx('button button--primary button--lg', styles.buttonPrimary)}
                        to="/docs/overview">
                        开始使用
                    </Link>
                    <Link
                        className={clsx('button button--secondary button--lg', styles.buttonSecondary)}
                        to="https://github.com/xiaoniucode/etp">
                        <svg className={styles.githubIcon} viewBox="0 0 24 24" width="20" height="20">
                            <path
                                fill="currentColor"
                                d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                        </svg>
                        GitHub
                    </Link>
                </div>
            </div>
        </header>
    );
}

const features = [
    {title: '管理面板', description: '提供直观易与操作的高颜值Web管理界面，简化配置操作，功能丰富，提供实时流量观测统计'},
    {title: '高性能传输', description: '基于Netty零拷贝机制、ProtoBuf等技术实现高性能数据传输,GraalVM原生编译实现毫秒级启动'},
    {title: '协议支持', description: '支持TCP协议以及TCP上层协议代理,TLS1.3高效加密协议,使用PKCS#12格式存储密钥与证书'},
    {title: '轻量小巧', description: '聚焦核心，资源占用少，零外部依赖，即装即用, 支持纯Toml静态配置或管理界面动态配置使用'},
    {title: '跨平台支持', description: '支持Linux、Windows、macOS、Docker，兼容ARM64、AMD64架构，支持SpringBoot生态集成'},
    {title: '功能丰富', description: '支持断线重连、流量观测统计、启停代理服务、端口池、多客户端、无客户端模式、客户端踢除等'},
];

export default function Home() {
    const {siteConfig} = useDocusaurusContext();

    return (
        <Layout title={siteConfig.title} description={siteConfig.tagline}>
            <HomepageHeader/>
            <main>
                <section className={styles.features}>
                    <div className="container">
                        <div className="row">
                            {features.map((feature, idx) => (
                                <div key={idx} className={clsx('col col--4', styles.featureCard)}>
                                    <div className="card">
                                        <div className="card__header">
                                            <h3>{feature.title}</h3>
                                        </div>
                                        <div className="card__body">
                                            <p>{feature.description}</p>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </section>
            </main>
        </Layout>
    );
}
