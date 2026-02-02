package com.jin.xianqu_backend.interceptor;

import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;
import java.util.List;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            // 设置不需要强制登录的路径（如果未登录也能访问，登录了则注入用户信息）
            List<String> optionalPaths = Arrays.asList(
                    "/goods/list",
                    "/goods/hotSearch",
                    "/post/list",
                    "/comment/list",
                    "/part-time-job/list",
                    "/lost-found/list");
            for (String path : optionalPaths) {
                if (request.getRequestURI().contains(path)) {
                    return true;
                }
            }

            // 放行详情页 GET 请求 (e.g. /goods/123)
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String uri = request.getRequestURI();
                // 匹配 /goods/数字, /post/数字 等
                if (uri.matches(".*(/(goods|post|part-time-job|lost-found)/\\d+)$")) {
                    return true;
                }
            }

            response.setStatus(401);
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        UserContext.remove();
    }
}
