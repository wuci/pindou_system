package com.pindou.timer.websocket;

import com.pindou.timer.dto.RemindInfo;

/**
 * WebSocket服务接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface WebsocketService {

    /**
     * 广播桌台状态变更
     *
     * @param tableId 桌台ID
     */
    void broadcastTableStatusChange(Integer tableId);

    /**
     * 广播提醒消息
     *
     * @param remind 提醒信息
     */
    void broadcastRemind(RemindInfo remind);

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    int getOnlineCount();
}
