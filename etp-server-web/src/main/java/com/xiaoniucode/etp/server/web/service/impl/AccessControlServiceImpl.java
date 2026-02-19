package com.xiaoniucode.etp.server.web.service.impl;

import com.xiaoniucode.etp.core.enums.AccessControlMode;
import com.xiaoniucode.etp.server.web.common.BizException;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.convert.AccessControlConvert;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.dto.AccessControlRuleDTO;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.AddAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRequest;
import com.xiaoniucode.etp.server.web.controller.accesscontrol.request.UpdateAccessControlRuleRequest;
import com.xiaoniucode.etp.server.web.entity.AccessControl;
import com.xiaoniucode.etp.server.web.entity.AccessControlRule;
import com.xiaoniucode.etp.server.web.repository.AccessControlRepository;
import com.xiaoniucode.etp.server.web.repository.AccessControlRuleRepository;
import com.xiaoniucode.etp.server.web.service.AccessControlService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 访问控制服务实现
 */
@Service
public class AccessControlServiceImpl implements AccessControlService {
    
    private final AccessControlRepository accessControlRepository;
    private final AccessControlRuleRepository accessControlRuleRepository;
    
    public AccessControlServiceImpl(AccessControlRepository accessControlRepository, 
                                   AccessControlRuleRepository accessControlRuleRepository) {
        this.accessControlRepository = accessControlRepository;
        this.accessControlRuleRepository = accessControlRuleRepository;
    }
    
    @Override
    public AccessControlDTO getByProxyId(String proxyId) {
        AccessControl accessControl = accessControlRepository.findByProxyId(proxyId);
        if (accessControl == null) {
            throw new BizException("根据proxyId未找到访问控制配置: " + proxyId);
        }
        
        AccessControlDTO dto = AccessControlConvert.INSTANCE.toDTO(accessControl);
        List<AccessControlRule> rules = accessControlRuleRepository.findByAcId(accessControl.getId());
        List<AccessControlRuleDTO> ruleDTOs = rules.stream()
                .map(AccessControlConvert.INSTANCE::toRuleDTO)
                .collect(Collectors.toList());
        dto.setRules(ruleDTOs);
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateAccessControlRequest request) {
        Integer id = request.getId();
        AccessControl accessControl = accessControlRepository.findById(id)
                .orElseThrow(() -> new BizException("根据ID未找到访问控制配置: " + id));
        
        accessControl.setEnable(request.getEnable());
        accessControl.setMode(AccessControlMode.fromCode(request.getMode()));
        accessControlRepository.save(accessControl);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRuleById(Integer ruleId) {
        if (!accessControlRuleRepository.existsById(ruleId)) {
            throw new BizException("根据ID未找到访问控制规则: " + ruleId);
        }
        accessControlRuleRepository.deleteById(ruleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddAccessControlRequest request) {
        if (accessControlRepository.findByProxyId(request.getProxyId()) != null) {
            throw new BizException("该proxyId的访问控制配置已存在: " + request.getProxyId());
        }
        AccessControl accessControl = AccessControlConvert.INSTANCE.toEntity(request);
        accessControlRepository.save(accessControl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRule(AddAccessControlRuleRequest request) {
        if (!accessControlRepository.existsById(request.getAcId())) {
            throw new BizException("根据ID未找到访问控制配置: " + request.getAcId());
        }
        AccessControlRule rule = AccessControlConvert.INSTANCE.toRuleEntity(request);
        accessControlRuleRepository.save(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(UpdateAccessControlRuleRequest request) {
        AccessControlRule rule = accessControlRuleRepository.findById(request.getId())
                .orElseThrow(() -> new BizException("根据ID未找到访问控制规则: " + request.getId()));
        rule.setRuleType(AccessControlMode.fromCode(request.getRuleType()));
        accessControlRuleRepository.save(rule);
    }
}
