package io.github.lxien.orbien.autoconfigure;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthProperties implements Serializable {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 客户端名称
     */
    private String name;
}
