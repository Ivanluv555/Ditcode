package com.design26.ditserver.core.error;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理器，捕获应用中抛出的ApiException和其他异常，返回统一格式的错误响应
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理ApiException，返回异常中定义的HTTP状态码和错误消息
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("ok", false, "message", ex.getMessage()));
    }

    // 处理请求参数验证失败的异常，返回400状态码和第一个验证错误的消息，如果没有具体错误则返回一个通用消息
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().isEmpty()
            ? "请求参数不合法"
            : ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("ok", false, "message", message));
    }

    // 处理其他未捕获的异常，返回500状态码和一个通用的错误消息，避免泄露内部错误细节
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("ok", false, "message", "服务器内部错误"));
    }
}
