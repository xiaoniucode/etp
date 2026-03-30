package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();

    @Override
    public Optional<TokenInfo> findByToken(String token) {
        return Optional.ofNullable(tokenMap.get(token));
    }

    @Override
    public TokenInfo getByToken(String token) {
        return tokenMap.get(token);
    }

    @Override
    public List<TokenInfo> findAll() {
        return new ArrayList<>(tokenMap.values());
    }

    @Override
    public boolean existsByToken(String token) {
        return tokenMap.containsKey(token);
    }

    @Override
    public TokenInfo save(TokenInfo token) {
        tokenMap.put(token.getToken(), token);
        return token;
    }

    @Override
    public TokenInfo deleteByToken(String token) {
       return tokenMap.remove(token);
    }
}