package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("sys_user")
@Schema(description = "用户")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "角色ID")
    @TableField("role_id")
    private String roleId;

    @Schema(description = "状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "最后登录时间（毫秒时间戳）")
    @TableField("last_login_at")
    private Long lastLoginAt;

    @Schema(description = "最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @Schema(description = "删除标记：0-未删除，1-已删除")
    @TableLogic
    @TableField("deleted_at")
    private Integer deletedAt;
}
