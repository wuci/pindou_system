package com.pindou.timer.config;

import com.pindou.timer.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 *
 * @author pindou
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 配置请求授权
            .authorizeRequests()

            // 放行Knife4j接口文档相关路径
            .antMatchers(
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/favicon.ico",
                "/**/api-docs",
                "/**/api-docs/**"
            ).permitAll()

            // 放行Druid监控相关路径
            .antMatchers("/druid/**").permitAll()

            // 放行公共接口（登录接口等）
            .antMatchers("/auth/**").permitAll()

            // 放行健康检查接口
            .antMatchers("/health/**").permitAll()

            // 其他所有请求都需要认证
            .anyRequest().authenticated()
            .and()

            // 禁用CSRF（使用JWT时不需要）
            .csrf().disable()

            // 基于token，所以不需要session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // 禁用CORS（已在WebMvcConfig中配置）
            .cors();

        return http.build();
    }
}
