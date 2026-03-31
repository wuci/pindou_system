package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 桌台信息响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "桌台信息响应")
public class TableInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "桌台编号")
    private Integer id;

    @Schema(description = "桌台名称")
    private String name;

    @Schema(description = "状态：idle-空闲 using-使用中 paused-暂停")
    private String status;

    @Schema(description = "当前订单ID")
    private String currentOrderId;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "已用时长（秒）")
    private Long duration;

    @Schema(description = "暂停时长（秒）")
    private Long pauseDuration;

    @Schema(description = "预设时长（秒）")
    private Integer presetDuration;

    @Schema(description = "当前费用（元）")
    private Double amount;

    @Schema(description = "是否已提醒：1-是 0-否")
    private Integer reminded;

    @Schema(description = "提醒是否被忽略：1-是 0-否")
    private Integer remindIgnored;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "到点时间（毫秒时间戳）")
    private Long endTime;
}
