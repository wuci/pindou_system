package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_member")
@Schema(description = "会员")
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "会员名称")
    @TableField("name")
    private String name;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "累计消费金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "会员余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "会员等级ID")
    @TableField("level_id")
    private Long levelId;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
