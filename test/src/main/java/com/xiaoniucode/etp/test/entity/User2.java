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

package com.xiaoniucode.etp.test.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuxin
 */
@Data
@Entity
@Table(name = "user2")
@Accessors(chain = true)
public class User2 {
    /**
     * 用户ID
     */
    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 用户名
     */
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 密码
     */
    @Column(name = "password", length = 512, nullable = false)
    private String password;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 50)
    private String email;
    /**
     * 备注
     */
    @Column(name = "remark", length = 50)
    private String remark;
}
