package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息响应DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "用户信息响应")
public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "最后登录时间（毫秒时间戳）")
    private Long lastLoginAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;
}
