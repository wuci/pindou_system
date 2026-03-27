package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
