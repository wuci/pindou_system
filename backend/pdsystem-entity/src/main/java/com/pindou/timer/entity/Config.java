package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置实体类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@TableName("sys_config")
@Schema(description = "系统配置")
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "配置键")
    @TableField("config_key")
    private String configKey;

    @Schema(description = "配置值（JSON字符串）")
    @TableField("config_value")
    private String configValue;

    @Schema(description = "配置描述")
    @TableField("description")
    private String description;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField("updated_at")
    private Long updatedAt;

    @Schema(description = "更新人ID")
    @TableField("updated_by")
    private String updatedBy;
}
