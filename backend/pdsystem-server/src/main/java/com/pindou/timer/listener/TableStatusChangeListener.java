package com.pindou.timer.listener;

import com.pindou.timer.event.TableStatusChangeEvent;
import com.pindou.timer.websocket.WebsocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 桌台状态变更事件监听器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Component
public class TableStatusChangeListener {

    private static final Logger log = LoggerFactory.getLogger(TableStatusChangeListener.class);

    @Resource
    private WebsocketService websocketService;

    /**
     * 监听桌台状态变更事件，推送 WebSocket 消息
     */
    @EventListener
    public void onTableStatusChange(TableStatusChangeEvent event) {
        Integer tableId = event.getTableId();
        log.debug("监听到桌台状态变更事件: tableId={}", tableId);

        // 推送 WebSocket 消息
        websocketService.broadcastTableStatusChange(tableId);
    }
}
