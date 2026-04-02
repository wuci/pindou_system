package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员充值请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "会员充值请求")
public class RechargeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "充值金额", required = true)
    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "支付方式", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "备注")
    private String remark;
}
