package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色查询请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "角色查询请求")
public class RoleQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码，从1开始")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "角色名称（模糊查询）")
    private String name;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
