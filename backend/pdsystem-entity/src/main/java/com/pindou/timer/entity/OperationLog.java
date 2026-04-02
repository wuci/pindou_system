package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志实体类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@TableName("sys_operation_log")
@Schema(description = "操作日志")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日志ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "操作模块")
    @TableField("module")
    private String module;

    @Schema(description = "操作类型")
    @TableField("operation")
    private String operation;

    @Schema(description = "操作描述")
    @TableField("description")
    private String description;

    @Schema(description = "操作用户ID")
    @TableField("user_id")
    private String userId;

    @Schema(description = "操作用户名")
    @TableField("username")
    private String username;

    @Schema(description = "请求方法")
    @TableField("method")
    private String method;

    @Schema(description = "请求参数")
    @TableField("params")
    private String params;

    @Schema(description = "响应结果")
    @TableField("result")
    private String result;

    @Schema(description = "执行时长（毫秒）")
    @TableField("duration")
    private Long duration;

    @Schema(description = "客户端IP")
    @TableField("ip")
    private String ip;

    @Schema(description = "用户代理")
    @TableField("user_agent")
    private String userAgent;

    @Schema(description = "操作状态：0-失败，1-成功")
    @TableField("status")
    private Integer status;

    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "操作内容（兼容旧表结构的 content 字段）")
    @TableField("content")
    private String content;

    @Schema(description = "目标类型")
    @TableField("target_type")
    private String targetType;

    @Schema(description = "目标ID")
    @TableField("target_id")
    private String targetId;

    @Schema(description = "操作时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;
}
