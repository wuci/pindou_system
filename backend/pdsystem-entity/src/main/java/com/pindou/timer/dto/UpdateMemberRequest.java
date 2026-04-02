package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 更新会员请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "更新会员请求")
public class UpdateMemberRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员名称", required = true)
    @NotBlank(message = "会员名称不能为空")
    private String name;

    @Schema(description = "地址")
    private String address;
}
