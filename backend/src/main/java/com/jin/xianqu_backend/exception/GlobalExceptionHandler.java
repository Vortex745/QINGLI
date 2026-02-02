package com.jin.xianqu_backend.exception;

import com.jin.xianqu_backend.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理静态资源不存在异常 (HTTP 404)
     * 对于图片资源请求，返回空 404 以避免前端渲染层解析 JSON 报错
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public org.springframework.http.ResponseEntity<?> noResourceFoundExceptionHandler(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.warn("静态资源不存在: {}", e.getResourcePath());

        // 如果是图片类请求，不返回 JSON 结果体，避免前端渲染层报错
        String path = e.getResourcePath().toLowerCase();
        if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".webp")
                || path.endsWith(".gif")) {
            return org.springframework.http.ResponseEntity.status(404).build();
        }

        return org.springframework.http.ResponseEntity.status(404)
                .body(Result.error(404, "资源不存在: " + e.getResourcePath()));
    }

    /**
     * 处理系统异常 (HTTP 500)
     */
    @ExceptionHandler(Throwable.class)
    public org.springframework.http.ResponseEntity<?> runtimeExceptionHandler(Throwable e,
            jakarta.servlet.http.HttpServletRequest request) {
        // Fallback: 如果 NoResourceFoundException 被包装或漏网
        if (e instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            log.warn("静态资源不存在 (Caught in generic): {}", e.getMessage());
            return org.springframework.http.ResponseEntity.status(404).build();
        }

        log.error("系统异常:", e);

        // 同样检查 URI，如果是图片请求，避免返回 JSON
        String uri = request.getRequestURI().toLowerCase();
        if (uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".png") || uri.endsWith(".webp")
                || uri.endsWith(".gif")) {
            return org.springframework.http.ResponseEntity.status(500).build();
        }

        return org.springframework.http.ResponseEntity.status(500)
                .body(Result.error(500, "系统错误: " + e.getClass().getName() + ": " + e.getMessage()));
    }
}
