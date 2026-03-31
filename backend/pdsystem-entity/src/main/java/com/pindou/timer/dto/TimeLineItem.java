package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 时间线记录项DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "时间线记录项")
public class TimeLineItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "操作时间（毫秒时间戳）")
    private Long time;

    @Schema(description = "操作动作：start-开始 pause-暂停 resume-恢复 end-结束")
    private String action;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作人")
    private String operator;
}
