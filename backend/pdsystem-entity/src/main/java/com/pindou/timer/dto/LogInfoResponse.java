package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 日志信息响应DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "日志信息响应")
public class LogInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID")
    private String id;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "响应结果")
    private String result;

    @Schema(description = "执行时长（毫秒）")
    private Long duration;

    @Schema(description = "客户端IP")
    private String ip;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "操作时间（毫秒时间戳）")
    private Long createdAt;
}
