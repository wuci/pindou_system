package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 余额消费记录实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_consumption_record")
@Schema(description = "余额消费记录")
public class ConsumptionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消费记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "会员ID")
    @TableField("member_id")
    private Long memberId;

    @Schema(description = "会员姓名")
    @TableField("member_name")
    private String memberName;

    @Schema(description = "会员手机号")
    @TableField("member_phone")
    private String memberPhone;

    @Schema(description = "关联订单ID")
    @TableField("order_id")
    private String orderId;

    @Schema(description = "消费金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "消费前余额")
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    @Schema(description = "消费后余额")
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
}
