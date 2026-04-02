package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_member_level")
@Schema(description = "会员等级")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "等级ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "等级名称")
    @TableField("name")
    private String name;

    @Schema(description = "最小累计金额")
    @TableField("min_amount")
    private BigDecimal minAmount;

    @Schema(description = "最大累计金额（null表示无上限）")
    @TableField("max_amount")
    private BigDecimal maxAmount;

    @Schema(description = "折扣率（0.9表示9折）")
    @TableField("discount_rate")
    private BigDecimal discountRate;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
