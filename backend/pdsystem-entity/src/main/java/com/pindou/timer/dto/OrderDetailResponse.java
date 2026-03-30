package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "订单详情响应")
public class OrderDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private String id;

    @Schema(description = "桌台编号")
    private Integer tableId;

    @Schema(description = "桌台名称")
    private String tableName;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "支付时间（毫秒时间戳）")
    private Long paidAt;

    @Schema(description = "总时长（秒）")
    private Integer duration;

    @Schema(description = "暂停总时长（秒）")
    private Integer pauseDuration;

    @Schema(description = "实际使用时长（秒）")
    private Integer actualDuration;

    @Schema(description = "预设时长（秒）")
    private Integer presetDuration;

    @Schema(description = "状态：active-进行中 completed-已完成")
    private String status;

    @Schema(description = "总金额")
    private BigDecimal amount;

    @Schema(description = "正常费用")
    private BigDecimal normalAmount;

    @Schema(description = "超时费用")
    private BigDecimal overtimeAmount;

    @Schema(description = "操作员ID")
    private String operatorId;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;

    @Schema(description = "时间线记录")
    private List<TimeLineItem> timeLine;

    @Data
    @Schema(description = "时间线记录")
    public static class TimeLineItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "时间（毫秒时间戳）")
        private Long time;

        @Schema(description = "操作类型")
        private String action;

        @Schema(description = "操作说明")
        private String description;

        @Schema(description = "操作人")
        private String operator;
    }
}
