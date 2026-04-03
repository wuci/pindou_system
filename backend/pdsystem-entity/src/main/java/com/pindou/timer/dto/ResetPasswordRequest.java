package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 重置密码请求DTO
 *
 * @author wuci
 * @date 2026-04-03
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "新密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在 6 到 20 个字符")
    private String password;
}
