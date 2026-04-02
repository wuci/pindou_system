package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员简略信息DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "会员简略信息")
public class MemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "会员名称")
    private String name;

    @Schema(description = "会员等级名称")
    private String levelName;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "折后金额")
    private BigDecimal finalAmount;

    @Schema(description = "会员余额")
    private BigDecimal balance;
}
