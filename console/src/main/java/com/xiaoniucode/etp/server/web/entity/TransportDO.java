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

package com.xiaoniucode.etp.server.web.entity;

import com.xiaoniucode.etp.core.enums.TunnelType;
import com.xiaoniucode.etp.server.web.entity.converter.TunnelTypeConverter;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transport")
public class TransportDO {
    @Id
    private String proxyId;
    /**
     * 是否启用TLS加密
     */
    @Column(name = "encrypt", nullable = false)
    private Boolean encrypt;
    /**
     * 隧道类型
     */
    @Column(name = "tunnel_type", nullable = false)
    @Convert(converter = TunnelTypeConverter.class)
    private TunnelType tunnelType;
}
