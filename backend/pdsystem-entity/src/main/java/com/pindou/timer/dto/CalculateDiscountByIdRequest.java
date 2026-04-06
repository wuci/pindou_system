package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 根据折扣ID计算折扣请求DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "根据折扣ID计算折扣请求")
public class CalculateDiscountByIdRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "折扣ID", required = true)
    @NotBlank(message = "折扣ID不能为空")
    private String discountId;

    @Schema(description = "订单金额", required = true)
    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    @Schema(description = "会员ID")
    private Long memberId;
}
