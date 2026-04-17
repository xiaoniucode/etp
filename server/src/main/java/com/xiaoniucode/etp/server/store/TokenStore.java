package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import com.xiaoniucode.etp.server.exceptions.EtpException;

import java.util.List;

public interface TokenStore {
    TokenConfig findByToken(String token);

    TokenConfig getByToken(String token);

    List<TokenConfig> findAll();

    boolean existsByToken(String token);

    boolean existsByName(String name);

    TokenConfig add(TokenConfig token) throws EtpException;

    TokenConfig update(TokenConfig tokenConfig) throws EtpException;

    TokenConfig deleteByToken(String token);
}