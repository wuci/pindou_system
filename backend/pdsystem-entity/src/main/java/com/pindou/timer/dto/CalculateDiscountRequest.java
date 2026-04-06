package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计算折扣请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "计算折扣请求")
public class CalculateDiscountRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单金额", required = true)
    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    @Schema(description = "会员等级ID")
    private Long memberLevelId;
}
