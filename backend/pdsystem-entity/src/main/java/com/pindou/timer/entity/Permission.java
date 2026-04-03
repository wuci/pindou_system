package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限配置实体类
 * 用于存储系统的权限树结构
 *
 * @author wuci
 * @date 2026-04-03
 */
@Data
@TableName("sys_permission")
@Schema(description = "权限配置")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "父权限ID，顶级节点为root")
    @TableField("parent_id")
    private String parentId;

    @Schema(description = "权限编码（唯一标识，如 table:view）")
    @TableField("permission_key")
    private String permissionKey;

    @Schema(description = "权限名称（显示名称）")
    @TableField("permission_name")
    private String permissionName;

    @Schema(description = "权限类型：module-模块，permission-权限项")
    @TableField("permission_type")
    private String permissionType;

    @Schema(description = "图标名称")
    @TableField("icon")
    private String icon;

    @Schema(description = "路由路径（可选）")
    @TableField("path")
    private String path;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "是否内置：0-否，1-是（内置权限不可删除）")
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
