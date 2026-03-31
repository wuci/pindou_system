package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 今日统计数据响应DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "今日统计数据响应")
public class DailyStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日营收（元）")
    private Double todayRevenue;

    @Schema(description = "使用中桌台数")
    private Integer activeTableCount;

    @Schema(description = "今日订单数")
    private Integer todayOrderCount;

    @Schema(description = "翻台率")
    private Double turnoverRate;

    @Schema(description = "今日总时长（秒）")
    private Long todayDuration;
}
