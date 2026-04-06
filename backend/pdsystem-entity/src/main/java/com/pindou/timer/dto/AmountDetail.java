package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 金额明细DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "金额明细")
public class AmountDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "正常费用（元）")
    private Double normalAmount;

    @Schema(description = "超时费用（元）")
    private Double overtimeAmount;

    @Schema(description = "总费用（元）")
    private Double totalAmount;

    @Schema(description = "计费时长（秒）")
    private Integer actualDuration;

    @Schema(description = "计费方式：hour-按小时 minute-按分钟")
    private String billingType;

    @Schema(description = "单价")
    private Double unitPrice;

    @Schema(description = "超时费率")
    private Double overtimeRate;
}
