package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户信息DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "用户信息")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID（UUID）")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "最后登录时间（毫秒时间戳）")
    private Long lastLoginAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;
}
