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
    public <T> T getAndPut(String cacheName, String key, Supplier<T> dbLoader) {
        Cache l1 = getL1Cache(cacheName);
        Cache l2 = getL2Cache(cacheName);

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
        putInternal(l1, l2, key, value);

        return value;
    }


    @SuppressWarnings("all")
    public <T> T getCache(String cacheName, String key) {
        return getCache(cacheName, key, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCache(String cacheName, String key, Class<T> clazz) {
        Cache l1 = getL1Cache(cacheName);
        Cache l2 = getL2Cache(cacheName);

        Object value = null;

        // 优先查 L1
        if (l1 != null) {
            Cache.ValueWrapper wrapper = l1.get(key);
            if (wrapper != null) {
                value = wrapper.get();
            }
        }

        // L1 未命中则查 L2
        if (value == null && l2 != null) {
            Cache.ValueWrapper wrapper = l2.get(key);
            if (wrapper != null) {
                value = wrapper.get();
            }
        }

        if (value == null) {
            return null;
        }

        if (clazz != null && !clazz.isInstance(value)) {
            return null;
        }

        return (T) value;
    }

    /**
     * 手动写入缓存（同时写入 L1 + L2）
     */
    public void putCache(String cacheName, String key, Object value) {
        if (value == null) {
            return;
        }

        Cache l1 = getL1Cache(cacheName);
        Cache l2 = getL2Cache(cacheName);

        putInternal(l1, l2, key, value);
    }

    private void putInternal(Cache l1, Cache l2, String key, Object value) {
        if (value == null) return;

        if (l1 != null) {
            l1.put(key, value);
        }
        if (l2 != null) {
            l2.put(key, value);
        }
    }

    private Cache getL1Cache(String cacheName) {
        return l1CacheManager.getCache(cacheName);
    }

    private Cache getL2Cache(String cacheName) {
        return (l2CacheManager != null) ? l2CacheManager.getCache(cacheName) : null;
    }

    /**
     * 删除缓存（L1 + L2）
     */
    public void evict(String cacheName, String key) {
        Cache l1 = getL1Cache(cacheName);
        if (l1 != null) {
            l1.evict(key);
        }

        Cache l2 = getL2Cache(cacheName);
        if (l2 != null) {
            l2.evict(key);
        }
    }

    /**
     * 清空缓存（L1 + L2）
     */
    public void evictAll(String cacheName) {
        Cache l1 = getL1Cache(cacheName);
        if (l1 != null) {
            l1.clear();
        }

        Cache l2 = getL2Cache(cacheName);
        if (l2 != null) {
            l2.clear();
        }
    }
}