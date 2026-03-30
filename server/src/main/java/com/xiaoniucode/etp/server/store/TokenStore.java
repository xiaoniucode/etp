package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenInfo;

import java.util.List;
import java.util.Optional;

public interface TokenStore {
    Optional<TokenInfo> findByToken(String token);
   TokenInfo getByToken(String token);

    List<TokenInfo> findAll();

    boolean existsByToken(String token);

    TokenInfo save(TokenInfo token);

    TokenInfo deleteByToken(String token);
}