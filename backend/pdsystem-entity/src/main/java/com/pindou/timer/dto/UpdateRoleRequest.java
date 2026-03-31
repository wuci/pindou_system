package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 更新角色请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "更新角色请求")
public class UpdateRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID", required = true)
    @NotBlank(message = "角色ID不能为空")
    private String id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "描述")
    private String description;
}
