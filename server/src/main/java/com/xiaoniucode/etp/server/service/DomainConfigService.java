/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaoniucode.etp.server.service;


import com.xiaoniucode.etp.server.config.AppConfig;
import com.xiaoniucode.etp.server.service.repository.DomainQueryRepository;
import com.xiaoniucode.etp.server.utils.DomainUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DomainConfigService {
    @Autowired
    private DomainQueryRepository domainQueryRepository;

    @Resource
    private AppConfig appConfig;

    /**
     * 检查域名是否存在
     *
     * @param fullDomain 完整域名
     * @return true 如果域名已存在，false 如果域名不存在
     */
    public boolean exists(String fullDomain) {
        String baseDomain = appConfig.getBaseDomain();

        if (!StringUtils.hasText(baseDomain)) {
            return existsCustomDomain(fullDomain);
        }
        String prefix = DomainUtils.extractPrefix(fullDomain, baseDomain);

        if (prefix == null) {
            return existsCustomDomain(fullDomain);
        }

        return existsSubdomain(baseDomain, prefix);
    }

    public boolean existsCustomDomain(String customDomain) {
        return domainQueryRepository.existsByDomain(customDomain);
    }

    public boolean existsSubdomain(String baseDomain, String domain) {
        return domainQueryRepository.existsBySubdomain(baseDomain, domain);
    }
}
