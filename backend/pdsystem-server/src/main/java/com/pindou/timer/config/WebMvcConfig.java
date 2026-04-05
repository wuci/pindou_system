package com.pindou.timer.config;

import com.pindou.timer.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置类
 *
 * @author pindou
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private PermissionInterceptor permissionInterceptor;

    /**
     * 配置静态资源处理
     * 将前端构建后的静态资源映射到 classpath:/static/ 目录
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 检查静态资源是否存在
        org.springframework.core.io.Resource resource = new ClassPathResource("static/index.html");
        if (resource.exists()) {
            // 静态资源存在，启用前端资源映射
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .setCachePeriod(3600); // 缓存1小时
        }
    }

    /**
     * 配置跨域
     *
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源
                .allowedOriginPatterns("*")
                // 允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的头
                .allowedHeaders("*")
                // 允许携带凭证
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/captcha",
                        "/api/health/**",
                        "/error"
                );
    }
}
