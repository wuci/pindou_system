package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 创建会员请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "创建会员请求")
public class CreateMemberRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员名称", required = true)
    @NotBlank(message = "会员名称不能为空")
    private String name;

    @Schema(description = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "地址")
    private String address;
}
