package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限配置响应DTO
 *
 * @author wuci
 * @date 2026-04-03
 */
@Data
@Schema(description = "权限配置响应")
public class PermissionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    private String id;

    @Schema(description = "父权限ID")
    private String parentId;

    @Schema(description = "权限编码")
    private String permissionKey;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限类型：module-模块，permission-权限项")
    private String permissionType;

    @Schema(description = "图标名称")
    private String icon;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "是否内置：0-否，1-是")
    private Integer isBuiltIn;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "更新时间")
    private Long updatedAt;

    @Schema(description = "子权限列表")
    private List<PermissionResponse> children;
}
