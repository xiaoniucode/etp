package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DomainGenerator {
    private final Logger logger = LoggerFactory.getLogger(DomainGenerator.class);
    private final String[] baseDomains;
    private final DomainManager domainManager;

    public DomainGenerator(Set<String> baseDomains, DomainManager domainManager) {
        this.domainManager = domainManager;
        String[] newArray = baseDomains.toArray(new String[0]);
        this.baseDomains = newArray;
        logger.debug("基础域名数组已刷新，长度：{}", newArray.length);
    }

    /**
     * 优先级：自定义域名--子域名--自动生成域名
     *
     * @param config 代理配置信息
     * @return 可用域名列表
     */
    public Set<String> generate(ProxyConfig config) {
        Set<String> res = new HashSet<>();
        if (baseDomains.length == 0) {
            return ConcurrentHashMap.newKeySet();
        }
        Set<String> customDomains = config.getCustomDomains();

        //1.处理自定义域名
        if (customDomains != null && !customDomains.isEmpty()) {
            for (String domain : customDomains) {
                if (domainManager.exist(domain)) {
                    logger.warn("自定义域名已被占用：{}", domain);
                    continue;
                }
                res.add(domain);
            }
        }

        //2.处理子域名
        Set<String> subPrefixes = config.getSubDomains();
        if (subPrefixes != null && !subPrefixes.isEmpty()) {
            for (String prefix : subPrefixes) {
                String trimmed = prefix.trim();
                if (trimmed.isEmpty()) continue;

                // 随机选择一个基础域名
                String randomBase = baseDomains[ThreadLocalRandom.current().nextInt(baseDomains.length)];
                String candidate = trimmed.toLowerCase() + "." + randomBase;

                candidate = normalizeDomain(candidate);

                if (domainManager.exist(candidate)) {
                    logger.warn("子域名已被占用：{}", candidate);
                    continue;
                }

                res.add(candidate);
                logger.debug("生成子域名成功：{}", candidate);
            }
        }
        //3.根据基础域名自动生成域名
        Boolean autoGenerate = config.getAutoDomain();
        if (Boolean.TRUE.equals(autoGenerate)) {
            String randomPrefix = generateRandomPrefix(2, 10);
            String randomBase = baseDomains[ThreadLocalRandom.current().nextInt(baseDomains.length)];
            String autoDomain = randomPrefix + "." + randomBase;

            autoDomain = normalizeDomain(autoDomain);

            if (!domainManager.exist(autoDomain)) {
                res.add(autoDomain);
                logger.debug("自动生成域名成功：{}", autoDomain);
            } else {
                logger.warn("自动生成的域名已被占用：{}", autoDomain);
            }
        }
        return res;
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return "";
        }
        String d = domain.trim().toLowerCase();
        d = d.replaceAll("^\\.+", "").replaceAll("\\.+$", "");
        return d;
    }

    /**
     * 生成随机前缀（字母+数字）
     */
    private String generateRandomPrefix(int minLen, int maxLen) {
        int length = ThreadLocalRandom.current().nextInt(minLen, maxLen + 1);
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }
}
