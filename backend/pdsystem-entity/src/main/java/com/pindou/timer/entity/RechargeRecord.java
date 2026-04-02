package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充值记录实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_recharge_record")
@Schema(description = "充值记录")
public class RechargeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "充值记录ID")
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

    @Schema(description = "充值金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "充值前余额")
    @TableField("balance_before")
    private BigDecimal balanceBefore;

    @Schema(description = "充值后余额")
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @Schema(description = "支付方式")
    @TableField("payment_method")
    private String paymentMethod;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "操作员ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "操作员姓名")
    @TableField("operator_name")
    private String operatorName;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
}
