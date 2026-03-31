package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色信息响应DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "角色信息响应")
public class RoleInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "是否内置：0-否，1-是")
    private Integer isBuiltIn;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;
}
