package com.xiaoniucode.etp.server.store;

import com.xiaoniucode.etp.server.config.domain.TokenConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, TokenConfig> tokenMap = new ConcurrentHashMap<>();

    @Override
    public Optional<TokenConfig> findByToken(String token) {
        return Optional.ofNullable(tokenMap.get(token));
    }

    @Override
    public TokenConfig getByToken(String token) {
        return tokenMap.get(token);
    }

    @Override
    public List<TokenConfig> findAll() {
        return new ArrayList<>(tokenMap.values());
    }

    @Override
    public boolean existsByToken(String token) {
        return tokenMap.containsKey(token);
    }

    @Override
    public TokenConfig save(TokenConfig token) {
        tokenMap.put(token.getToken(), token);
        return token;
    }

    @Override
    public TokenConfig deleteByToken(String token) {
       return tokenMap.remove(token);
    }
}