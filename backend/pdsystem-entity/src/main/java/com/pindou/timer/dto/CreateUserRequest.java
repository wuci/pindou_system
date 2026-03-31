package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 创建用户请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "昵称", required = true)
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "角色ID", required = true)
    @NotNull(message = "角色不能为空")
    private String roleId;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status = 1;
}
