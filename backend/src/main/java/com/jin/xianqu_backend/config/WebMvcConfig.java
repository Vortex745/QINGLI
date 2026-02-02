package com.jin.xianqu_backend.config;

import com.jin.xianqu_backend.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/area/match",

                        "/health",
                        "/upload/**", // 放行静态资源映射路径
                        "/profile/**",
                        "/doc.html",
                        "/chat/system-notice", // 调试用
                        "/chat/read-notice/**", // 增加放行
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs/**",
                        // "/file/upload", // 已经不再强制要求免登录
                        "/error");
    }

    // 依然保留 CORS 配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Private-Network")
                .maxAge(3600);
    }

    // 静态资源映射 (本地文件上传)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 统一映射到公共上传目录
        // 针对 Windows 环境使用 file: 后接绝对路径
        String publicUploadPath = "file:E:/testJava/xianqu_uniapp/upload";

        System.out.println("---------- Static Resource Config ----------");
        System.out.println("Mapping /profile/** to: " + publicUploadPath);
        System.out.println("--------------------------------------------");

        // 注册资源处理器 (双向映射以处理不同的 context-path 行为)
        registry.addResourceHandler("/profile/**")
                .addResourceLocations(publicUploadPath + "/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/api/profile/**")
                .addResourceLocations(publicUploadPath + "/")
                .setCachePeriod(3600);
    }
}
