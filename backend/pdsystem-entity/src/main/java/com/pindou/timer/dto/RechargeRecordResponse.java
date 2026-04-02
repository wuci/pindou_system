package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充值记录响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "充值记录响应")
public class RechargeRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "充值记录ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "充值前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "充值后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付方式名称")
    private String paymentMethodName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private Long createdAt;
}
