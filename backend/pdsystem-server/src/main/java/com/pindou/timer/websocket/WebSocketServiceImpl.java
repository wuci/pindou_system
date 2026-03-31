package com.pindou.timer.websocket;

import com.pindou.timer.dto.RemindInfo;
import com.pindou.timer.dto.WebSocketMessage;
import com.pindou.timer.entity.Table;
import com.pindou.timer.mapper.TableMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * WebSocket服务实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebsocketService {

    @Resource
    private TableWebSocketHandler tableWebSocketHandler;

    @Resource
    private TableMapper tableMapper;

    @Override
    public void broadcastTableStatusChange(Integer tableId) {
        try {
            // 查询桌台信息
            Table table = tableMapper.selectById(tableId);
            if (table == null) {
                log.warn("桌台不存在: tableId={}", tableId);
                return;
            }

            // 构建消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.TABLE_STATUS)
                    .data(table)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 广播消息
            tableWebSocketHandler.broadcast(message);
            log.debug("广播桌台状态变更: tableId={}, status={}", tableId, table.getStatus());
        } catch (Exception e) {
            log.error("广播桌台状态变更失败: tableId={}", tableId, e);
        }
    }

    @Override
    public void broadcastRemind(RemindInfo remind) {
        try {
            // 构建消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.REMIND)
                    .data(remind)
                    .timestamp(System.currentTimeMillis())
                    .build();

            // 广播消息
            tableWebSocketHandler.broadcast(message);
            log.debug("广播提醒消息: tableId={}, type={}", remind.getTableId(), remind.getRemindType());
        } catch (Exception e) {
            log.error("广播提醒消息失败: tableId={}", remind.getTableId(), e);
        }
    }

    @Override
    public int getOnlineCount() {
        return tableWebSocketHandler.getOnlineCount();
    }
}
