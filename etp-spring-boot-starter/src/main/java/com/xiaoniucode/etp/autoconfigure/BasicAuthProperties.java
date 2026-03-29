package com.xiaoniucode.etp.autoconfigure;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BasicAuthProperties implements Serializable {
    /**
     * 是否启用基础认证
     */
    private boolean enabled = false;

    /**
     * 用户列表
     */
    private List<User> users = new ArrayList<>();

    @Data
    public static class User {
        /**
         * 用户名
         */
        private String user;

        /**
         * 密码
         */
        private String pass;
    }
}
