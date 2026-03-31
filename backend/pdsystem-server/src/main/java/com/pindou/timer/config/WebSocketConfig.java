package com.pindou.timer.config;

import com.pindou.timer.websocket.TableWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * WebSocket配置类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private TableWebSocketHandler tableWebSocketHandler;

    /**
     * WebSocket端点路径
     */
    private static final String WEB_SOCKET_ENDPOINT = "/ws";

    /**
     * 允许的源
     */
    private static final String ALLOWED_ORIGINS = "*";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tableWebSocketHandler, WEB_SOCKET_ENDPOINT)
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
}
