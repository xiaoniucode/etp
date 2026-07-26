/*
 *    Copyright 2026 lxien
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

package io.github.lxien.orbien.server.web.service;

import io.github.lxien.orbien.server.web.common.utils.JsonUtils;
import io.github.lxien.orbien.server.web.entity.AcmeCertOrderDO;
import io.github.lxien.orbien.server.web.entity.ProxyDomainDO;
import io.github.lxien.orbien.server.web.repository.AcmeCertOrderRepository;
import io.github.lxien.orbien.server.web.repository.ProxyDomainRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AcmeOrderBindSyncService {
    private final Logger logger = LoggerFactory.getLogger(AcmeOrderBindSyncService.class);

    private final AcmeCertOrderRepository acmeCertOrderRepository;
    private final ProxyDomainRepository proxyDomainRepository;

    /**
     * 按即将删除的代理域名主键，从所有订单的绑定列表中剔除对应 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void detachProxyDomains(Collection<Long> proxyDomainIds) {
        if (CollectionUtils.isEmpty(proxyDomainIds)) {
            return;
        }
        Set<Long> removed = new HashSet<>();
        for (Long id : proxyDomainIds) {
            if (id != null) {
                removed.add(id);
            }
        }
        if (removed.isEmpty()) {
            return;
        }

        List<AcmeCertOrderDO> orders = acmeCertOrderRepository.findByBindProxyDomainIdsIsNotNull();
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        int updated = 0;
        for (AcmeCertOrderDO order : orders) {
            if (!StringUtils.hasText(order.getBindProxyDomainIds())) {
                continue;
            }
            List<Long> current = JsonUtils.toLongList(order.getBindProxyDomainIds());
            if (CollectionUtils.isEmpty(current)) {
                continue;
            }
            List<Long> retained = new ArrayList<>(current.size());
            boolean changed = false;
            for (Long id : current) {
                if (id != null && removed.contains(id)) {
                    changed = true;
                    continue;
                }
                if (id != null) {
                    retained.add(id);
                }
            }
            if (!changed) {
                continue;
            }
            order.setBindProxyDomainIds(retained.isEmpty() ? null : JsonUtils.toJson(retained));
            acmeCertOrderRepository.save(order);
            updated++;
        }
        if (updated > 0) {
            logger.debug("已同步清理 ACME 订单域名绑定引用: removedDomainIds={}, updatedOrders={}",
                    removed.size(), updated);
        }
    }

    /**
     * 按代理 ID 查找其域名记录并剥离 ACME 绑定引用
     */
    @Transactional(rollbackFor = Exception.class)
    public void detachByProxyIds(Collection<String> proxyIds) {
        if (CollectionUtils.isEmpty(proxyIds)) {
            return;
        }
        List<String> ids = proxyIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        List<Long> domainIds = proxyDomainRepository.findByProxyIdIn(ids).stream()
                .map(ProxyDomainDO::getId)
                .toList();
        detachProxyDomains(domainIds);
    }
}
