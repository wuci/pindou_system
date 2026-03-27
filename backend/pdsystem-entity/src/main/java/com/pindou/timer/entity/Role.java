package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("sys_role")
@Schema(description = "角色")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "角色名称")
    @TableField("name")
    private String name;

    @Schema(description = "角色编码")
    @TableField("code")
    private String code;

    @Schema(description = "权限列表（JSON数组）")
    @TableField("permissions")
    private String permissions;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否内置：0-否，1-是")
    @TableField("is_built_in")
    private Integer isBuiltIn;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @Schema(description = "删除标记：0-未删除，1-已删除")
    @TableLogic
    @TableField("deleted_at")
    private Integer deletedAt;
}
