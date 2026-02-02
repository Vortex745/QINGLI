package com.jin.xianqu_backend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.xianqu_backend.common.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 接口请求日志切面
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 切入点：所有 Controller 方法
     */
    @Pointcut("execution(* com.jin.xianqu_backend.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String url = request != null ? request.getRequestURL().toString() : "";
        String method = request != null ? request.getMethod() : "";
        String ip = request != null ? request.getRemoteAddr() : "";
        String className = point.getTarget().getClass().getName();
        String methodName = point.getSignature().getName();
        Object[] args = point.getArgs();
        Long userId = UserContext.getUserId();

        log.info("Request Start: [{} {}] IP: {}, User: {}, Method: {}.{}, Args: {}",
                method, url, ip, userId, className, methodName, Arrays.toString(args));

        Object result = point.proceed();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Request End: [{} {}] Duration: {}ms, User: {}", method, url, duration, userId);

        return result;
    }
}
