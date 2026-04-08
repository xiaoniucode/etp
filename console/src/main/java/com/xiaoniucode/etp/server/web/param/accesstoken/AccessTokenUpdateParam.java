/*
 *    Copyright 2026 xiaoniucode
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http:
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaoniucode.etp.server.web.param.accesstoken;
import lombok.Getter;
import lombok.Setter;
/**
 * 更新访问令牌请求对象
 */
@Getter
@Setter
public class AccessTokenUpdateParam {
    /**
     * 访问令牌ID
     */
    private Integer id;
    /**
     * 令牌名称
     */
    private String name;
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
}