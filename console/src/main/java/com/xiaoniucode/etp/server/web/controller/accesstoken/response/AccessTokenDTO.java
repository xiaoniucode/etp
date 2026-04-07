package com.xiaoniucode.etp.server.web.controller.accesstoken.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AccessTokenDTO {
    private Integer id;
    /**
     * 令牌名称
     */
    private String name;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 最大设备数
     */
    private Integer maxDevice;

    /**
     * 设备超时时间
     */
    private Integer deviceTimeout;

    /**
     * 最大连接数
     */
    private Integer maxConnection;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
