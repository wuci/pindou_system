package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 日志查询请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "日志查询请求")
public class LogQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码，从1开始")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "状态：0-失败，1-成功")
    private Integer status;
}
