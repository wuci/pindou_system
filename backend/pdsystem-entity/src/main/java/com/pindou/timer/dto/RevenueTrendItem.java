package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 营收趋势项DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "营收趋势项")
public class RevenueTrendItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日期（格式：MM-dd）")
    private String date;

    @Schema(description = "营收（元）")
    private Double revenue;

    @Schema(description = "订单数")
    private Integer orderCount;
}
