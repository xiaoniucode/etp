package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;

import java.util.List;
import java.util.Optional;

public interface TokenStore {
    Optional<TokenConfig> findByToken(String token);
   TokenConfig getByToken(String token);

    List<TokenConfig> findAll();

    boolean existsByToken(String token);

    TokenConfig save(TokenConfig token);

    TokenConfig deleteByToken(String token);
}