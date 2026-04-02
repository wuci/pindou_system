package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_order")
@Schema(description = "订单")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "桌台ID")
    @TableField("table_id")
    private Integer tableId;

    @Schema(description = "桌台名称")
    @TableField("table_name")
    private String tableName;

    @Schema(description = "开始时间（毫秒时间戳）")
    @TableField("start_time")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    @TableField("end_time")
    private Long endTime;

    @Schema(description = "总时长（秒）")
    @TableField("duration")
    private Integer duration;

    @Schema(description = "暂停总时长（秒）")
    @TableField("pause_duration")
    private Integer pauseDuration;

    @Schema(description = "预设时长（秒）")
    @TableField("preset_duration")
    private Integer presetDuration;

    @Schema(description = "订餐渠道")
    @TableField("channel")
    private String channel;

    @Schema(description = "状态：active-进行中 completed-已完成")
    @TableField("status")
    private String status;

    @Schema(description = "总金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "金额明细（JSON）")
    @TableField("amount_detail")
    private String amountDetail;

    @Schema(description = "操作员ID")
    @TableField("operator_id")
    private String operatorId;

    @Schema(description = "操作员姓名")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "会员ID")
    @TableField("member_id")
    private Long memberId;

    @Schema(description = "原价（折扣前）")
    @TableField("original_amount")
    private BigDecimal originalAmount;

    @Schema(description = "支付时间（毫秒时间戳）")
    @TableField("paid_at")
    private Long paidAt;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
