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

package com.xiaoniucode.etp.server.web.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiaoniucode.etp.server.web.support.store.MultiLevelCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({
        CaffeineCacheProperties.class,
        RedisCacheProperties.class
})
public class CacheConfig {

    /**
     * L1：Caffeine 本地缓存
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager(CaffeineCacheProperties props) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(props.getInitialCapacity())
                .maximumSize(props.getMaximumSize())
                .expireAfterAccess(props.getExpireAfterAccess()));
        return manager;
    }

    /**
     * L2：Redis（可选）
     */
    @Bean
    @ConditionalOnProperty(name = "cache.redis.enabled", havingValue = "true")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory,RedisCacheProperties props) {
        ObjectMapper objectMapper = new ObjectMapper();

        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(props.getDefaultTtl())
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
    @Bean
    public MultiLevelCache multiLevelCache(
            CaffeineCacheManager caffeineCacheManager,
            ObjectProvider<RedisCacheManager> redisProvider) {

        return new MultiLevelCache(
                caffeineCacheManager,
                redisProvider.getIfAvailable()
        );
    }
}