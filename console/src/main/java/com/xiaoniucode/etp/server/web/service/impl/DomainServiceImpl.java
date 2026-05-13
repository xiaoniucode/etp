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

import com.xiaoniucode.etp.server.web.common.message.PageQuery;
import com.xiaoniucode.etp.server.web.common.message.PageResult;
import com.xiaoniucode.etp.server.web.dto.domain.DomainDTO;
import com.xiaoniucode.etp.server.web.entity.DomainDO;
import com.xiaoniucode.etp.server.web.repository.DomainRepository;
import com.xiaoniucode.etp.server.web.service.DomainService;
import com.xiaoniucode.etp.server.web.service.converter.DomainConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DomainServiceImpl implements DomainService {
    @Autowired
    private DomainRepository domainRepository;
    @Autowired
    private DomainConvert domainConvert;

    @Override
    public PageResult<DomainDTO> findByPage(PageQuery pageQuery) {
        int currentPage = Math.max(0, pageQuery.getCurrent() - 1);
        Pageable pageable = PageRequest.of(currentPage, pageQuery.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DomainDO> resultPage = domainRepository.findAll(pageable);
        if (resultPage.isEmpty()) {
            return PageResult.empty(pageQuery.getCurrent(), pageQuery.getSize());
        }
        List<DomainDTO> dtoList = domainConvert.toDTOList(resultPage.getContent());
        return PageResult.wrap(resultPage, dtoList);
    }

    @Override
    public DomainDTO getById(Integer id) {
        DomainDO domainDO = domainRepository.findById(id).orElse(null);
        if (domainDO == null) {
            return null;
        }
        return domainConvert.toDTO(domainDO);
    }
}
