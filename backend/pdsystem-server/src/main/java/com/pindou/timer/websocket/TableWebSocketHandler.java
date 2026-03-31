package com.pindou.timer.websocket;

import cn.hutool.json.JSONUtil;
import com.pindou.timer.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Component
public class TableWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储所有在线的WebSocket会话
     * Key: sessionId, Value: WebSocketSession
     */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 存储用户ID与会话ID的映射
     * Key: userId, Value: sessionId
     */
    private static final Map<String, String> USER_SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);

        // 从URI中获取userId（例如：/ws?userId=123）
        String uri = session.getUri() != null ? session.getUri().toString() : "";
        String userId = extractUserId(uri);

        if (userId != null) {
            USER_SESSION_MAP.put(userId, sessionId);
            log.info("WebSocket连接建立: userId={}, sessionId={}", userId, sessionId);

            // 发送连接成功消息
            sendWelcomeMessage(session, userId);
        } else {
            log.warn("WebSocket连接建立但无法获取用户ID: sessionId={}", sessionId);
        }

        log.info("当前在线连接数: {}", SESSIONS.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: sessionId={}, message={}", session.getId(), payload);

        // 处理客户端发送的消息（如心跳）
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);

        // 从用户会话映射中移除
        USER_SESSION_MAP.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));

        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, status);
        log.info("当前在线连接数: {}", SESSIONS.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);

        // 发生错误时移除会话
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);
        USER_SESSION_MAP.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
    }

    /**
     * 广播消息给所有连接
     *
     * @param message 消息内容
     */
    public void broadcast(WebSocketMessage message) {
        String jsonMessage = JSONUtil.toJsonStr(message);
        TextMessage textMessage = new TextMessage(jsonMessage);

        SESSIONS.forEach((sessionId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送WebSocket消息失败: sessionId={}", sessionId, e);
                }
            }
        });

        log.debug("广播消息给所有连接: type={}, count={}", message.getType(), SESSIONS.size());
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public void sendToUser(String userId, WebSocketMessage message) {
        String sessionId = USER_SESSION_MAP.get(userId);
        if (sessionId == null) {
            log.warn("用户不在线: userId={}", userId);
            return;
        }

        WebSocketSession session = SESSIONS.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = JSONUtil.toJsonStr(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("发送消息给用户: userId={}, type={}", userId, message.getType());
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 获取当前在线连接数
     *
     * @return 在线连接数
     */
    public int getOnlineCount() {
        return SESSIONS.size();
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        String sessionId = USER_SESSION_MAP.get(userId);
        if (sessionId == null) {
            return false;
        }

        WebSocketSession session = SESSIONS.get(sessionId);
        return session != null && session.isOpen();
    }

    /**
     * 从URI中提取用户ID
     *
     * @param uri 请求URI
     * @return 用户ID
     */
    private String extractUserId(String uri) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }

        // 从查询参数中获取userId
        String[] parts = uri.split("\\?");
        if (parts.length < 2) {
            return null;
        }

        String queryString = parts[1];
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                return keyValue[1];
            }
        }

        return null;
    }

    /**
     * 发送欢迎消息
     *
     * @param session WebSocket会话
     * @param userId  用户ID
     */
    private void sendWelcomeMessage(WebSocketSession session, String userId) throws IOException {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessage.MessageType.SYSTEM)
                .data("连接成功，欢迎来到拼豆计时系统")
                .timestamp(System.currentTimeMillis())
                .build();

        String jsonMessage = JSONUtil.toJsonStr(message);
        session.sendMessage(new TextMessage(jsonMessage));
    }
}
