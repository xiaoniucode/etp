package com.xiaoniucode.etp.common.config;

public interface ConfigSource {
    Config load() ;
    ConfigSourceType getSourceType();
}
