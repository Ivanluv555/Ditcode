package com.design26.ditserver.core.error;

import org.springframework.http.HttpStatus;

// API异常类，封装HTTP状态码和错误消息，供全局异常处理器使用
public class ApiException extends RuntimeException {
    // 一个HttpStatus类型的状态
    private final HttpStatus status;

    // 构造函数，接受一个HttpStatus和一个错误消息，调用父类构造函数设置消息，并保存状态
    public ApiException(HttpStatus status, String message) {
        // super来自RuntimeException
        super(message);
        this.status = status;
    }

    // 一个简单的getter方法
    public HttpStatus getStatus() {
        return status;
    }
}
