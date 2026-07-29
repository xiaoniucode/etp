/*
 *
 *  *    Copyright 2026 lxien
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package io.github.lxien.orbien.server.web.entity;

import io.github.lxien.orbien.server.web.entity.converter.BindStatusConverter;
import io.github.lxien.orbien.server.web.enums.BindStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 证书与代理域名绑定
 */
@Data
@Entity
@Table(name = "cert_domain_binding", indexes = {
        @Index(name = "idx_cert_binding_cert_id", columnList = "cert_id"),
        @Index(name = "idx_cert_binding_domain", columnList = "domain")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cert_binding_proxy_domain", columnNames = "proxy_domain_id")
})
public class CertDomainBinding {
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 代理域名ID
     */
    @Column(name = "proxy_domain_id", nullable = false)
    private Long proxyDomainId;
    /**
     * 证书ID
     */
    @Column(name = "cert_id", nullable = false)
    private String certId;
    /**
     * 绑定域名
     */
    @Column(name = "domain", nullable = false)
    private String domain;
    /**
     * 绑定状态
     */
    @Convert(converter = BindStatusConverter.class)
    @Column(name = "status", nullable = false)
    private BindStatus status;
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    /**
     * 部署版本号
     */
    @Column(name = "deploy_version")
    private Integer deployVersion = 0;
    /**
     * 最近部署时间
     */
    @Column(name = "last_deployed_at")
    private LocalDateTime lastDeployedAt;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
