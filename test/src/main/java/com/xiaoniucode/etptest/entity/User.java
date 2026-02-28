package com.xiaoniucode.etptest.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author liuxin
 */
@Data
@Entity
@Table(name = "users")
@Accessors(chain = true)
public class User  {
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
