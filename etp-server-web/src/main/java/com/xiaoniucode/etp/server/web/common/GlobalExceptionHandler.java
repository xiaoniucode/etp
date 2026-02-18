package com.xiaoniucode.etp.server.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Ajax handleBusinessException(BizException e) {
        return Ajax.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Ajax handleException(Exception e) {
        logger.error(e.getMessage());
        return Ajax.error(e.getMessage());
    }

}
