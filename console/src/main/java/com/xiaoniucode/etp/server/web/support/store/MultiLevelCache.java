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

package com.xiaoniucode.etp.server.web.support.store;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.function.Supplier;

/**
 * 多级缓存
 */

/**
 * 多级缓存（L1 + L2，可选）
 */
public class MultiLevelCache {

    private final CacheManager l1CacheManager;
    private final CacheManager l2CacheManager;

    public MultiLevelCache(CacheManager l1CacheManager, CacheManager l2CacheManager) {
        this.l1CacheManager = l1CacheManager;
        this.l2CacheManager = l2CacheManager;
    }

    /**
     * 查询流程：L1 -> L2 -> DB，并自动回填缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key, Supplier<T> dbLoader) {

        Cache l1 = l1CacheManager.getCache(cacheName);
        Cache l2 = (l2CacheManager != null) ? l2CacheManager.getCache(cacheName) : null;

        // 查 L1
        if (l1 != null) {
            Cache.ValueWrapper wrapper = l1.get(key);
            if (wrapper != null) {
                return (T) wrapper.get();
            }
        }

        // 查 L2（Redis）
        if (l2 != null) {
            Cache.ValueWrapper wrapper = l2.get(key);
            if (wrapper != null) {
                T value = (T) wrapper.get();

                // 回填 L1
                if (value != null && l1 != null) {
                    l1.put(key, value);
                }

                return value;
            }
        }

        // 查 DB
        T value = dbLoader.get();

        // 写入 L1 + L2
        if (value != null) {
            if (l1 != null) {
                l1.put(key, value);
            }
            if (l2 != null) {
                l2.put(key, value);
            }
        }

        return value;
    }

    /**
     * 删除缓存（L1 + L2）
     */
    public void evict(String cacheName, String key) {

        Cache l1 = l1CacheManager.getCache(cacheName);
        if (l1 != null) {
            l1.evict(key);
        }

        if (l2CacheManager != null) {
            Cache l2 = l2CacheManager.getCache(cacheName);
            if (l2 != null) {
                l2.evict(key);
            }
        }
    }

    /**
     * 清空缓存（L1 + L2）
     */
    public void evictAll(String cacheName) {

        Cache l1 = l1CacheManager.getCache(cacheName);
        if (l1 != null) {
            l1.clear();
        }

        if (l2CacheManager != null) {
            Cache l2 = l2CacheManager.getCache(cacheName);
            if (l2 != null) {
                l2.clear();
            }
        }
    }
}