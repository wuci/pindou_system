package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新权限配置请求DTO
 *
 * @author wuci
 * @date 2026-04-03
 */
@Data
@Schema(description = "更新权限配置请求")
public class UpdatePermissionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    @NotBlank(message = "权限ID不能为空")
    private String id;

    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

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
