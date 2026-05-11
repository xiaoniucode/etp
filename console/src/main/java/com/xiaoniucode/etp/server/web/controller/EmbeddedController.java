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

package com.xiaoniucode.etp.server.web.controller;

import com.xiaoniucode.etp.server.web.common.message.Ajax;
import com.xiaoniucode.etp.server.web.param.proxy.embedded.EmbeddedBatchDeleteParam;
import com.xiaoniucode.etp.server.web.service.EmbeddedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/embedded")
public class EmbeddedController {
    @Autowired
    private EmbeddedService embeddedService;

    @GetMapping
    public Ajax listByPage(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Ajax.success(embeddedService.listByPage(page, size));
    }

    @GetMapping("{proxyId}")
    public Ajax detail(@PathVariable String proxyId) {
        return Ajax.success(embeddedService.detail(proxyId));
    }

    @DeleteMapping("batch")
    public Ajax batchDelete(@RequestBody @Validated EmbeddedBatchDeleteParam param) {
        embeddedService.batchDelete(param.getAgentIds());
        return Ajax.success();
    }
}
