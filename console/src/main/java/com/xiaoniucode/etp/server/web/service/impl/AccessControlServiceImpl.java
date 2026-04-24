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
import com.xiaoniucode.etp.core.enums.AccessControl;
import com.xiaoniucode.etp.server.service.his.ProxyManager;
import com.xiaoniucode.etp.server.security.IpAccessChecker;
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
import java.util.Objects;

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
    @Autowired
    private IpAccessChecker ipAccessChecker;

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
                AccessControlConfig ac = proxyConfig.getOrCreateAccessControlConfig();
                ac.setMode(accessControlDO.getMode());
                ac.setEnabled(accessControlDO.getEnabled());
                proxyConfig.setAccessControl(ac);
                ipAccessChecker.invalidate(proxyId);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRuleById(Long id) {
        AccessControlRuleDO accessControlRuleDO = accessControlRuleRepository.findById(id)
                .orElseThrow(() -> new BizException("规则不存在"));

        String proxyId = accessControlRuleDO.getProxyId();
        accessControlRuleRepository.deleteById(id);

        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                AccessControlConfig ac = proxyConfig.getOrCreateAccessControlConfig();
                switch (accessControlRuleDO.getMode()) {
                    case ALLOW -> ac.removeAllow(accessControlRuleDO.getCidr());
                    case DENY -> ac.removeDeny(accessControlRuleDO.getCidr());
                }
                proxyConfig.setAccessControl(ac);
                ipAccessChecker.invalidate(proxyId);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRule(AccessControlRuleAddParam param) {
        String proxyId = param.getProxyId();
        accessControlRepository.findById(proxyId).orElseThrow(() -> new BizException("访问控制配置不出在"));
        if (accessControlRuleRepository.existsByProxyIdAndCidr(proxyId, param.getCidr())) {
            throw new BizException("已存在相同CIDR规则");
        }

        AccessControlRuleDO accessControlRuleDO = accessControlConvert.toRuleDO(param);
        accessControlRuleRepository.save(accessControlRuleDO);
        //更新缓存
        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                AccessControlConfig ac = proxyConfig.getOrCreateAccessControlConfig();
                switch (accessControlRuleDO.getMode()) {
                    case ALLOW -> ac.addAllow(accessControlRuleDO.getCidr());
                    case DENY -> ac.addDeny(accessControlRuleDO.getCidr());
                }
                proxyConfig.setAccessControl(ac);
                ipAccessChecker.invalidate(proxyId);
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(AccessControlRuleUpdateParam param) {
        AccessControlRuleDO ruleDO = accessControlRuleRepository.findById(param.getId())
                .orElseThrow(() -> new BizException("规则不存在"));
        //如果没有变化直接返回
        if (Objects.equals(ruleDO.getMode().getCode(), param.getRuleType())
                && Objects.equals(param.getCidr(), ruleDO.getCidr())) {
            return;
        }
        //如果cidr变化需要检查当前代理下是否存在相同的，但排除自己
        if (!Objects.equals(ruleDO.getCidr(), param.getCidr())) {
            boolean exists = accessControlRuleRepository
                    .existsByProxyIdAndCidrAndIdNot(
                            ruleDO.getProxyId(),
                            param.getCidr(),
                            param.getId()
                    );
            if (exists) {
                throw new BizException("该规则已存在");
            }
        }
        String proxyId = ruleDO.getProxyId();
        //旧配置
        String oldCidr = ruleDO.getCidr();
        AccessControl oldRule = ruleDO.getMode();
        //更新数据库
        accessControlConvert.updateRuleDO(ruleDO, param);
        accessControlRuleRepository.save(ruleDO);

        AccessControl newRule = ruleDO.getMode();
        //更新缓存
        transactionHelper.afterCommit(() -> {
            proxyManager.findById(proxyId).ifPresent(proxyConfig -> {
                AccessControlConfig ac = proxyConfig.getOrCreateAccessControlConfig();
                //删除旧的
                switch (oldRule) {
                    case ALLOW -> ac.removeAllow(oldCidr);
                    case DENY -> ac.removeDeny(oldCidr);
                }
                switch (newRule) {
                    case ALLOW -> ac.addAllow(ruleDO.getCidr());
                    case DENY -> ac.addDeny(ruleDO.getCidr());
                }
                proxyConfig.setAccessControl(ac);
                ipAccessChecker.invalidate(proxyId);
            });
        });
    }
}
