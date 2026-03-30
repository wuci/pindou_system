package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("sys_config")
@Schema(description = "系统配置")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "配置键")
    @TableField("config_key")
    private String configKey;

    @Schema(description = "配置值（JSON格式）")
    @TableField("config_value")
    private String configValue;

    @Schema(description = "配置描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
