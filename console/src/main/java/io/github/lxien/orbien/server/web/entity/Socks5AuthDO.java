package io.github.lxien.orbien.server.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SOCKS5认证开关
 */
@Data
@Entity
@Table(name = "socks5_auth")
@NoArgsConstructor
@AllArgsConstructor
public class Socks5AuthDO {
    /**
     * 代理ID
     */
    @Id
    @Column(name = "proxy_id")
    private String proxyId;
    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled;
}
