package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计算折扣响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "计算折扣响应")
public class CalculateDiscountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原价")
    private BigDecimal originalAmount;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "折后金额")
    private BigDecimal finalAmount;
}
