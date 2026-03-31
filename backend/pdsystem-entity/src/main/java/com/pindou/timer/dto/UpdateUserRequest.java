package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新用户请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
