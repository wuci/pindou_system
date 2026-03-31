package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建角色请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "创建角色请求")
public class CreateRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色名称", required = true)
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色编码", required = true)
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;

    @Schema(description = "描述")
    private String description;
}
