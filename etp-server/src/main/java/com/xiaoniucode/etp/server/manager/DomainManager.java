package com.xiaoniucode.etp.server.manager;

import com.xiaoniucode.etp.core.enums.ProxyStatus;
import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.config.domain.ProxyConfig;
import com.xiaoniucode.etp.server.manager.domain.DomainInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DomainManager {
    private final Logger logger = LoggerFactory.getLogger(DomainManager.class);
    /**
     * domain -> DomainInfo
     */
    private final Map<String, DomainInfo> domainMap = new ConcurrentHashMap<>();
    private final Set<String> baseDomains = new HashSet<>();

    public DomainManager(AppConfig config) {
        if (config.getBaseDomains() != null) {
            baseDomains.addAll(config.getBaseDomains());
            logger.info("DomainManager初始化完成，基础域名: {}", baseDomains);
        }
    }

    public void addDomain(String domain, ProxyConfig proxyConfig) {
        if (domain == null || domain.isEmpty()) {
            logger.warn("尝试添加空域名");
            return;
        }

        if (proxyConfig == null) {
            logger.warn("尝试添加域名但代理配置为null: {}", domain);
            return;
        }

        domainMap.put(domain, new DomainInfo(domain, proxyConfig));
        logger.debug("添加域名: {} 到代理: {}", domain, proxyConfig.getName());
    }

    public void addDomains(Collection<String> domains, ProxyConfig proxyConfig) {
        if (domains == null || domains.isEmpty()) {
            return;
        }

        for (String domain : domains) {
            addDomain(domain, proxyConfig);
        }
    }

    public DomainInfo getDomainInfo(String domain) {
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setProxyStatus(ProxyStatus.OPEN);
        return new DomainInfo("a.domain1.com", proxyConfig);
        //todo return domainMap.get(domain);
    }

    public void removeDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return;
        }

        DomainInfo removed = domainMap.remove(domain);
        if (removed != null) {
            logger.debug("移除域名: {}", domain);
        }
    }

    public void removeDomains(Collection<String> domains) {
        if (domains == null || domains.isEmpty()) {
            return;
        }

        for (String domain : domains) {
            removeDomain(domain);
        }
    }

    public void removeDomainsByProxy(ProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return;
        }

        List<String> domainsToRemove = new ArrayList<>();
        for (Map.Entry<String, DomainInfo> entry : domainMap.entrySet()) {
            if (entry.getValue().getProxyConfig().equals(proxyConfig)) {
                domainsToRemove.add(entry.getKey());
            }
        }

        removeDomains(domainsToRemove);
        logger.debug("移除代理 {} 的所有域名", proxyConfig.getName());
    }

    public ProxyConfig getProxyConfigByDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }

        DomainInfo domainInfo = domainMap.get(domain);
        if (domainInfo != null && domainInfo.isActive()) {
            return domainInfo.getProxyConfig();
        }

        return null;
    }

    public boolean containsDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        DomainInfo domainInfo = domainMap.get(domain);
        return domainInfo != null && domainInfo.isActive();
    }

    public Set<String> getAllDomains() {
        Set<String> activeDomains = new HashSet<>();
        for (Map.Entry<String, DomainInfo> entry : domainMap.entrySet()) {
            if (entry.getValue().isActive()) {
                activeDomains.add(entry.getKey());
            }
        }
        return activeDomains;
    }

    public Set<String> getDomainsByProxy(ProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return Collections.emptySet();
        }

        Set<String> domains = new HashSet<>();
        for (Map.Entry<String, DomainInfo> entry : domainMap.entrySet()) {
            if (entry.getValue().getProxyConfig().equals(proxyConfig) && entry.getValue().isActive()) {
                domains.add(entry.getKey());
            }
        }
        return domains;
    }

    public void deactivateDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return;
        }

        DomainInfo domainInfo = domainMap.get(domain);
        if (domainInfo != null) {
            domainInfo.setActive(false);
            domainInfo.getProxyConfig().setProxyStatus(ProxyStatus.OPEN);
            logger.debug("停用域名: {}", domain);
        }
    }

    public void activateDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return;
        }

        DomainInfo domainInfo = domainMap.get(domain);
        if (domainInfo != null) {
            domainInfo.setActive(true);
            domainInfo.getProxyConfig().setProxyStatus(ProxyStatus.OPEN);
            logger.debug("启用域名: {}", domain);
        }
    }

    public Set<String> getBaseDomains() {
        return Collections.unmodifiableSet(baseDomains);
    }

    public void addBaseDomain(String baseDomain) {
        if (baseDomain == null || baseDomain.isEmpty()) {
            return;
        }

        baseDomains.add(baseDomain);
        logger.debug("添加基础域名: {}", baseDomain);
    }

    public void removeBaseDomain(String baseDomain) {
        if (baseDomain == null || baseDomain.isEmpty()) {
            return;
        }

        baseDomains.remove(baseDomain);
        logger.debug("移除基础域名: {}", baseDomain);
    }

    public boolean isBaseDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        return baseDomains.contains(domain);
    }

    public String generateAutoDomain(ProxyConfig proxyConfig) {
        Set<String> autoDomains = generateAutoDomains(proxyConfig, 1);
        return autoDomains.isEmpty() ? null : autoDomains.iterator().next();
    }

    public Set<String> generateAutoDomains(ProxyConfig proxyConfig, int count) {
        Set<String> autoDomains = new HashSet<>();

        if (proxyConfig == null || !Boolean.TRUE.equals(proxyConfig.getAutoDomain())) {
            return autoDomains;
        }

        if (baseDomains.isEmpty()) {
            String errorMsg = "未配置基础域名，无法生成自动域名";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        String proxyName = proxyConfig.getName();
        if (proxyName == null || proxyName.isEmpty()) {
            String errorMsg = "代理名称为空，无法生成自动域名";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 确保生成数量至少为1
        int generateCount = Math.max(1, count);

        // 将基础域名转换为列表以便随机访问
        List<String> baseDomainList = new ArrayList<>(baseDomains);
        Random random = new Random();

        // 生成指定数量的域名，随机选择基础域名
        int attemptCount = 0;
        int maxAttempts = generateCount * 10; // 最多尝试10倍数量，防止无限循环

        while (autoDomains.size() < generateCount && attemptCount < maxAttempts) {
            // 随机选择一个基础域名
            String baseDomain = baseDomainList.get(random.nextInt(baseDomainList.size()));
            String autoDomain = generateShortestAvailableDomain(baseDomain);

            if (isValidDomain(autoDomain) && !containsDomain(autoDomain)) {
                autoDomains.add(autoDomain);
                addDomain(autoDomain, proxyConfig);
                logger.debug("为代理 {} 生成自动域名: {}", proxyName, autoDomain);
            } else {
                logger.warn("为基础域名 {} 生成自动域名失败", baseDomain);
            }
            attemptCount++;
        }

        return autoDomains;
    }

    private String generateShortestAvailableDomain(String baseDomain) {
        // 从1位开始尝试生成
        for (int length = 1; length <= 10; length++) { // 最多尝试10位
            // 尝试纯数字
            String numericDomain = generateRandomDomain(length, baseDomain, "0123456789");
            if (numericDomain != null && !containsDomain(numericDomain)) {
                return numericDomain;
            }

            // 尝试纯小写字母
            String alphaDomain = generateRandomDomain(length, baseDomain, "abcdefghijklmnopqrstuvwxyz");
            if (alphaDomain != null && !containsDomain(alphaDomain)) {
                return alphaDomain;
            }

            // 尝试字母数字组合
            String alphanumericDomain = generateRandomDomain(length, baseDomain, "abcdefghijklmnopqrstuvwxyz0123456789");
            if (alphanumericDomain != null && !containsDomain(alphanumericDomain)) {
                return alphanumericDomain;
            }
        }

        // 如果所有尝试都失败，使用时间戳作为最后的手段
        return "auto" + System.currentTimeMillis() + "." + baseDomain;
    }

    private String generateRandomDomain(int length, String baseDomain, String chars) {
        // 最多尝试100次
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                int randomIndex = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(randomIndex));
            }
            String domain = sb + "." + baseDomain;
            if (!containsDomain(domain)) {
                return domain;
            }
        }
        return null;
    }


    private String generateValidPrefix(String proxyName) {
        // 移除特殊字符，只保留字母、数字和连字符
        String prefix = proxyName.toLowerCase().replaceAll("[^a-z0-9-]", "-");

        // 移除首尾的连字符
        prefix = prefix.replaceAll("^-+|-+$", "");

        // 如果前缀为空，使用默认前缀
        if (prefix.isEmpty()) {
            prefix = "auto" + System.currentTimeMillis();
        }

        // 限制前缀长度
        if (prefix.length() > 63) {
            prefix = prefix.substring(0, 63);
        }

        return prefix;
    }

    private boolean isValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        // 域名长度检查
        if (domain.length() > 253) {
            return false;
        }

        // 域名格式检查
        String domainRegex = "^([a-zA-Z0-9_-]+\\.)*[a-zA-Z0-9_-]+\\.[a-zA-Z]{2,}$";
        if (!domain.matches(domainRegex)) {
            return false;
        }

        // 检查每个标签
        String[] labels = domain.split("\\.");
        for (String label : labels) {
            if (label.length() > 63) {
                return false;
            }
            if (label.startsWith("-") || label.endsWith("-")) {
                return false;
            }
        }

        return true;
    }

    public Set<String> generateSubDomains(ProxyConfig proxyConfig) {
        Set<String> subDomains = new HashSet<>();

        if (proxyConfig == null || proxyConfig.getSubDomains() == null || proxyConfig.getSubDomains().isEmpty()) {
            return subDomains;
        }

        if (baseDomains.isEmpty()) {
            String errorMsg = "未配置基础域名，无法生成子域名";
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // 将基础域名转换为列表以便随机访问
        List<String> baseDomainList = new ArrayList<>(baseDomains);
        Random random = new Random();

        // 为每个子域名前缀生成完整域名，随机选择基础域名
        for (String subDomainPrefix : proxyConfig.getSubDomains()) {
            // 随机选择一个基础域名
            String baseDomain = baseDomainList.get(random.nextInt(baseDomainList.size()));
            String subDomain = subDomainPrefix + "." + baseDomain;

            // 检查域名是否有效且不重复
            if (isValidDomain(subDomain) && !containsDomain(subDomain)) {
                subDomains.add(subDomain);
                addDomain(subDomain, proxyConfig);
                logger.debug("为代理 {} 生成子域名: {}", proxyConfig.getName(), subDomain);
            } else {
                logger.warn("子域名已存在或无效: {}", subDomain);
            }
        }

        return subDomains;
    }

    public void clear() {
        domainMap.clear();
        logger.debug("清空所有域名");
    }

    public int size() {
        int count = 0;
        for (DomainInfo domainInfo : domainMap.values()) {
            if (domainInfo.isActive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 根据配置智能添加域名，优先级customDomains->subDomains->autoDomain
     *
     * @param proxyConfig 代理配置信息
     * @return 域名列表
     */
    public Set<String> addDomainsSmartly(ProxyConfig proxyConfig) {
        Set<String> domains = new HashSet<>();

        if (proxyConfig == null) {
            throw new IllegalStateException("代理配置为空");
        }

        // 1. 处理 customDomains（优先级最高）
        if (proxyConfig.getCustomDomains() != null && !proxyConfig.getCustomDomains().isEmpty()) {
            for (String customDomain : proxyConfig.getCustomDomains()) {
                if (isValidDomain(customDomain) && !containsDomain(customDomain)) {
                    domains.add(customDomain);
                    addDomain(customDomain, proxyConfig);
                    logger.debug("为代理 {} 添加自定义域名: {}", proxyConfig.getName(), customDomain);
                } else {
                    logger.warn("自定义域名已存在或无效: {}", customDomain);
                }
            }
            return domains;
        }

        // 2. 处理 subDomains（优先级次之）
        if (proxyConfig.getSubDomains() != null && !proxyConfig.getSubDomains().isEmpty()) {
            if (baseDomains.isEmpty()) {
                String errorMsg = "未配置基础域名，无法生成子域名";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            Set<String> subDomains = generateSubDomains(proxyConfig);
            domains.addAll(subDomains);
            return domains;
        }

        // 3. 处理 autoDomain（优先级最低）
        if (Boolean.TRUE.equals(proxyConfig.getAutoDomain())) {
            if (baseDomains.isEmpty()) {
                String errorMsg = "未配置基础域名，无法生成自动域名";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            String autoDomain = generateAutoDomain(proxyConfig);
            if (autoDomain != null) {
                domains.add(autoDomain);
            }
        }

        return domains;
    }

    /**
     * 检查域名是否可用
     *
     * @param domain
     * @return
     */
    public boolean checkAvailable(String domain) {
        return true;
    }

    /**
     * 判断所有域名是否可用
     *
     * @param domains
     * @return
     */
    public boolean isAllDomainsAvailable(Set<String> domains) {
        return true;
    }

    /**
     * 判断某个域名是否可用
     *
     * @param domain
     * @return
     */
    public boolean isDomainAvailable(String domain) {
        return true;
    }

    /**
     * 获取可用域名
     *
     * @param domains
     * @return
     */
    public Set<String> getAllAvailableDomains(Set<String> domains) {
        return domains;
    }

    /**
     * 获取不可用域名
     *
     * @param domains
     * @return
     */
    public Set<String> getUnavailableDomains(Set<String> domains) {
        return domains;
    }

    /**
     * 获取被其他代理占用的域名
     *
     * @param domains
     * @return
     */
    public Set<String> getOccupiedDomains(Set<String> domains) {
        return domains;
    }

    /**
     * 获取无效域名，格式错误
     *
     * @param domains
     * @return
     */
    public Set<String> getInvalidDomains(Set<String> domains) {
        return domains;
    }
}
