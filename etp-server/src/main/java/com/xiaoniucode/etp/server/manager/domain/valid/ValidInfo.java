package com.xiaoniucode.etp.server.manager.domain.valid;

import com.xiaoniucode.etp.core.domain.ProxyConfig;
import lombok.Getter;

/**
 * 代理配置注册校验信息
 */
@Getter
public class ValidInfo {
    private final boolean valid;
    private final ValidType type;
    private final String message;
    private ValidInfo(Builder builder) {
        this.valid = builder.valid;
        this.type = builder.type;
        this.message = builder.message;
    }

    public static ValidInfo ok() {
        return new Builder().valid(true).type(ValidType.NEW).build();
    }

    public static ValidInfo update(ProxyConfig existing) {
        return new Builder()
                .valid(true)
                .type(ValidType.UPDATE)
                .message("配置已存在，将进行更新")
                .build();
    }
    public static ValidInfo fail(String message) {
        return new Builder()
                .valid(false)
                .type(ValidType.INVALID)
                .message(message)
                .build();
    }
    public boolean isNew() {
        return type == ValidType.NEW;
    }
    public boolean isInValid(){
        return type==ValidType.INVALID;
    }

    public boolean isUpdate() {
        return type == ValidType.UPDATE;
    }
    public static class Builder {
        private boolean valid;
        private ValidType type = ValidType.NEW;
        private String message;
        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder type(ValidType type) {
            this.type = type;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }
        public ValidInfo build() {
            return new ValidInfo(this);
        }
    }
}

