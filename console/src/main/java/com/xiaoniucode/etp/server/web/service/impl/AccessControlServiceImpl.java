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

package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.domain.AccessControlConfig;
import com.xiaoniucode.etp.server.registry.ProxyManager;
import com.xiaoniucode.etp.server.web.common.exception.BizException;
import com.xiaoniucode.etp.server.web.dto.accesscontrol.AccessControlDetailDTO;
import com.xiaoniucode.etp.server.web.entity.AccessControlDO;
import com.xiaoniucode.etp.server.web.entity.AccessControlRuleDO;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleAddParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlRuleUpdateParam;
import com.xiaoniucode.etp.server.web.param.accesscontrol.AccessControlUpdateParam;
import com.xiaoniucode.etp.server.web.repository.AccessControlRepository;
import com.xiaoniucode.etp.server.web.repository.AccessControlRuleRepository;
import com.xiaoniucode.etp.server.web.service.AccessControlService;
import com.xiaoniucode.etp.server.web.service.converter.AccessControlConvert;
import com.xiaoniucode.etp.server.web.support.tx.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccessControlServiceImpl implements AccessControlService {
    @Autowired
    private AccessControlRepository accessControlRepository;
    @Autowired
    private AccessControlRuleRepository accessControlRuleRepository;
    @Autowired
    private AccessControlConvert accessControlConvert;
    @Autowired
    private ProxyManager proxyManager;
    @Autowired
    private TransactionHelper transactionHelper;

    @Override
    public AccessControlDetailDTO getByProxyId(String proxyId) {
        AccessControlDO accessControlDO = accessControlRepository.findById(proxyId)
                .orElseThrow(() -> new BizException("访问控制配置不存在"));
        List<AccessControlRuleDO> accessControlRuleDOS = accessControlRuleRepository.findByProxyId(proxyId);
        return accessControlConvert.toDetailDTO(accessControlDO, accessControlRuleDOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccessControl(AccessControlUpdateParam param) {
        String proxyId = param.getProxyId();
        AccessControlDO accessControlDO = accessControlRepository.findById(proxyId)
                .orElseThrow(() -> new BizException("访问控制配置不存在"));
        accessControlConvert.updateDO(accessControlDO, param);
        accessControlRepository.save(accessControlDO);
        //数据库事务提交以后更新缓存
        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                AccessControlConfig accessControl = proxyConfig.getOrCreateAccessControlConfig();
                accessControl.setMode(accessControlDO.getMode());
                accessControl.setEnabled(accessControlDO.getEnabled());
                proxyConfig.setAccessControl(accessControl);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRuleById(Long id) {
        accessControlRuleRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRule(AccessControlRuleAddParam param) {
        accessControlRepository.findById(param.getProxyId()).orElseThrow(()
                -> new BizException("访问控制配置不出在"));
        AccessControlRuleDO accessControlRuleDO = accessControlConvert.toRuleDO(param);
        accessControlRuleRepository.save(accessControlRuleDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(AccessControlRuleUpdateParam param) {
        AccessControlRuleDO accessControlRuleDO = accessControlRuleRepository.findById(param.getId()).orElseThrow(() -> new BizException("规则不存在"));
        accessControlConvert.updateRuleDO(accessControlRuleDO, param);
        accessControlRuleRepository.save(accessControlRuleDO);
    }
}
