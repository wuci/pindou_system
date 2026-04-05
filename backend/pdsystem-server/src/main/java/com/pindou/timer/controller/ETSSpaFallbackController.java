package com.pindou.timer.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * SPA (单页应用) 路由回退控制器
 * <p>
 * 用于处理前端 Vue Router 的 history 模式路由。
 * 当访问的路由不是 API 请求且静态资源不存在时，返回 index.html，
 * 让前端路由接管页面渲染。
 * </p>
 *
 * @author wuci
 * @date 2026-04-04
 */
@Controller
public class ETSSpaFallbackController {

    private static final String INDEX_HTML_PATH = "static/index.html";
    private String indexHtmlContent;
    private boolean spaEnabled = false;

    /**
     * 初始化时加载 index.html 内容
     */
    @PostConstruct
    public void init() {
        try {
            Resource resource = new ClassPathResource(INDEX_HTML_PATH);
            if (resource.exists()) {
                // 使用 InputStream 读取资源，兼容 JAR 包和文件系统
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    indexHtmlContent = reader.lines().collect(Collectors.joining("\n"));
                    spaEnabled = true;
                }
            }
        } catch (IOException e) {
            spaEnabled = false;
        }
    }

    /**
     * 处理根路径请求
     *
     * @return index.html 内容
     */
    @GetMapping("/")
    public ResponseEntity<String> index() {
        if (!spaEnabled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtmlContent);
    }

    /**
     * 处理前端路由回退
     * <p>
     * 当访问的路径不是 API 请求（不以 /api 开头）且静态资源不存在时，
     * 返回 index.html 让前端路由处理。
     * </p>
     *
     * @return index.html 内容
     */
    @RequestMapping(value = {"/dashboard/**", "/tables/**", "/orders/**", "/reports/**",
            "/users/**", "/roles/**", "/logs/**", "/members/**", "/member-levels/**", "/settings/**"})
    public ResponseEntity<String> fallback() {
        if (!spaEnabled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtmlContent);
    }

    /**
     * 处理登录页面
     *
     * @return index.html 内容
     */
    @GetMapping("/login")
    public ResponseEntity<String> login() {
        if (!spaEnabled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtmlContent);
    }
}
