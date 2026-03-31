package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 提醒信息DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "提醒信息")
public class RemindInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "桌台ID")
    private Integer tableId;

    @Schema(description = "桌台名称")
    private String tableName;

    @Schema(description = "提醒类型")
    private String remindType;

    @Schema(description = "提醒类型描述")
    private String remindTypeDesc;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "预设时长（秒）")
    private Integer presetDuration;

    @Schema(description = "已用时长（秒）")
    private Integer usedDuration;

    @Schema(description = "剩余时长（秒）")
    private Integer remainingDuration;

    @Schema(description = "超时时长（秒）")
    private Integer overtimeDuration;
}
