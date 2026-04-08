package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 订单信息响应DTO（用于列表展示）
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "订单信息响应")
public class OrderInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private String id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "父订单ID（NULL表示为父订单/独立订单）")
    private String parentId;

    @Schema(description = "续费次数（子订单数量）")
    private Integer extendCount;

    @Schema(description = "桌台编号")
    private Integer tableId;

    @Schema(description = "桌台名称")
    private String tableName;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "使用时长（秒）")
    private Integer duration;

    @Schema(description = "暂停时长（秒）")
    private Integer pauseDuration;

    @Schema(description = "预设时长（秒）")
    private Integer presetDuration;

    @Schema(description = "渠道：store-店内 meituan-美团 dianping-大众点评")
    private String channel;

    @Schema(description = "状态：active-进行中 completed-已完成 cancelled-已作废")
    private String status;

    @Schema(description = "当前费用（元）")
    private Double amount;

    @Schema(description = "原价（折扣前，元）")
    private Double originalAmount;

    @Schema(description = "折扣金额（元）")
    private Double discountAmount;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "会员等级名称")
    private String memberLevelName;

    @Schema(description = "会员折扣率")
    private Double memberDiscountRate;

    @Schema(description = "活动折扣ID")
    private String discountId;

    @Schema(description = "活动折扣名称")
    private String discountName;

    @Schema(description = "活动折扣率")
    private Double discountRate;

    @Schema(description = "活动折扣金额")
    private Double activityDiscountAmount;

    @Schema(description = "会员折扣金额")
    private Double memberDiscountAmount;

    @Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合")
    private String paymentMethod;

    @Schema(description = "余额支付金额（元）")
    private Double balanceAmount;

    @Schema(description = "其他方式支付金额（元）")
    private Double otherPaymentAmount;

    @Schema(description = "支付时间（毫秒时间戳）")
    private Long paidAt;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "子订单列表（仅父订单包含）")
    private List<OrderInfoResponse> childOrders;
}
