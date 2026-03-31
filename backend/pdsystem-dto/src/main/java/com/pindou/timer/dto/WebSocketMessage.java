package com.pindou.timer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static class MessageType {
        /**
         * 桌台状态变更
         */
        public static final String TABLE_STATUS = "table_status";

        /**
         * 提醒通知
         */
        public static final String REMIND = "remind";

        /**
         * 订单状态变更
         */
        public static final String ORDER_STATUS = "order_status";

        /**
         * 系统通知
         */
        public static final String SYSTEM = "system";
    }
}
