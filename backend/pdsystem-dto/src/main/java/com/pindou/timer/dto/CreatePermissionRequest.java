package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建权限配置请求DTO
 *
 * @author wuci
 * @date 2026-04-03
 */
@Data
@Schema(description = "创建权限配置请求")
public class CreatePermissionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "父权限ID，顶级节点为root")
    @NotBlank(message = "父权限ID不能为空")
    private String parentId;

    @Schema(description = "权限编码（如 table:view）")
    @NotBlank(message = "权限编码不能为空")
    private String permissionKey;

    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @Schema(description = "权限类型：module-模块，permission-权限项")
    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    @Schema(description = "图标名称")
    private String icon;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "描述")
    private String description;
}
