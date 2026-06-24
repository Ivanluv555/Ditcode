package com.design26.ditserver.core.error;

import org.springframework.http.HttpStatus;

/**
 * ApiException - 应用层统一的业务异常封装。
 * 业务角色：在控制器或服务层遇到可预期的业务错误时抛出，携带HTTP状态码与友好错误消息，
 * 由 GlobalExceptionHandler 捕获并转换为统一的HTTP响应。避免在业务代码中散落HTTP逻辑。
 */
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
