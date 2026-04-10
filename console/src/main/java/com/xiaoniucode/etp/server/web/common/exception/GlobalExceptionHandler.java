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

package com.xiaoniucode.etp.server.web.common.exception;
import com.xiaoniucode.etp.server.web.common.Ajax;
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
    @ExceptionHandler(SystemException.class)
    public Ajax handleSystemException(SystemException e) {
        return Ajax.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Ajax handleException(Exception e) {
        logger.error(e.getMessage());
        return Ajax.error(e.getMessage());
    }
}
