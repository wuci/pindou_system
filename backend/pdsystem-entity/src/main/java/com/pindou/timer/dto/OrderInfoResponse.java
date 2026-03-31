package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

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

    @Schema(description = "状态：active-进行中 completed-已完成")
    private String status;

    @Schema(description = "当前费用（元）")
    private Double amount;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "支付时间（毫秒时间戳）")
    private Long paidAt;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;
}
