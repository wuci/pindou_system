package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 计费规则配置DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "计费规则配置")
public class BillingRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "计费方式：hour-按小时 minute-按分钟")
    private String type;

    @Schema(description = "每小时单价（元）")
    private Double pricePerHour;

    @Schema(description = "每分钟单价（元）")
    private Double pricePerMinute;

    @Schema(description = "超时费率（倍数）")
    private Double overtimeRate;
}
