package com.textrpg.config;

import com.textrpg.common.BusinessException;
import com.textrpg.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBiz(BusinessException e) { return Result.fail(e.getMessage()); }

    @ExceptionHandler(Exception.class)
    public Result<?> handleAll(Exception e) {
        e.printStackTrace();
        return Result.fail("服务器内部错误: " + e.getMessage());
    }
}
