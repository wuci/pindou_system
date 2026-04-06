package com.pindou.timer.constants;

/**
 * 桌台常量类
 *
 * @author wuci
 * @date 2026-04-06
 */
public final class TableConstants {

    private TableConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /**
     * 桌台状态常量
     */
    public static final class Status {
        /** 空闲 */
        public static final String IDLE = "idle";
        /** 使用中 */
        public static final String USING = "using";
        /** 暂停 */
        public static final String PAUSED = "paused";

        private Status() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 预定状态常量
     */
    public static final class ReservationStatus {
        /** 无预定 */
        public static final String NONE = "none";
        /** 已预定 */
        public static final String RESERVED = "reserved";

        private ReservationStatus() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 订单状态常量
     */
    public static final class OrderStatus {
        /** 进行中 */
        public static final String ACTIVE = "active";
        /** 已完成 */
        public static final String COMPLETED = "completed";
        /** 已作废 */
        public static final String CANCELLED = "cancelled";

        private OrderStatus() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 支付方式常量
     */
    public static final class PaymentMethod {
        /** 线下支付 */
        public static final String OFFLINE = "offline";
        /** 余额支付 */
        public static final String BALANCE = "balance";
        /** 组合支付 */
        public static final String COMBINED = "combined";

        private PaymentMethod() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 渠道常量
     */
    public static final class Channel {
        /** 店内 */
        public static final String STORE = "store";
        /** 微信小程序 */
        public static final String WECHAT_MINI = "wechat_mini";
        /** 抖音小程序 */
        public static final String DOUYIN_MINI = "douyin_mini";

        private Channel() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 系统默认值
     */
    public static final class Defaults {
        /** 最小桌台数量 */
        public static final int MIN_TABLE_COUNT = 1;
        /** 最大桌台数量 */
        public static final int MAX_TABLE_COUNT = 50;
        /** 默认延长时间（分钟） */
        public static final int DEFAULT_EXTEND_TIME_MINUTES = 30;

        private Defaults() {
            throw new UnsupportedOperationException("常量类不允许实例化");
        }
    }

    /**
     * 判断是否为有效桌台状态（可以开始计时的状态）
     */
    public static boolean isValidForStartTimer(String status) {
        return Status.IDLE.equals(status);
    }

    /**
     * 判断是否为使用中状态（可以暂停、续费、结账的状态）
     */
    public static boolean isInUse(String status) {
        return Status.USING.equals(status) || Status.PAUSED.equals(status);
    }

    /**
     * 判断是否为空闲状态
     */
    public static boolean isIdle(String status) {
        return Status.IDLE.equals(status);
    }

    /**
     * 判断是否为可续费状态
     */
    public static boolean canExtend(String status) {
        return Status.USING.equals(status) || Status.PAUSED.equals(status);
    }

    /**
     * 判断是否为可结账状态
     */
    public static boolean canEndTimer(String status) {
        return Status.USING.equals(status) || Status.PAUSED.equals(status);
    }

    /**
     * 判断订单是否已完成
     */
    public static boolean isOrderCompleted(String status) {
        return OrderStatus.COMPLETED.equals(status);
    }

    /**
     * 判断是否为余额类支付
     */
    public static boolean isBalancePayment(String paymentMethod) {
        return PaymentMethod.BALANCE.equals(paymentMethod) || PaymentMethod.COMBINED.equals(paymentMethod);
    }
}
