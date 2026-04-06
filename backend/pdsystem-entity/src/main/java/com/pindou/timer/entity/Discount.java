package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 折扣设置实体类
 * 用于管理系统中的各种折扣活动
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@TableName("biz_discount")
@Schema(description = "折扣设置")
public class Discount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "折扣ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "折扣名称")
    @TableField("name")
    private String name;

    @Schema(description = "折扣类型：1-固定折扣 2-会员折扣 3-活动折扣")
    @TableField("type")
    private Integer type;

    @Schema(description = "折扣率（0.9表示9折，1.0表示不打折）")
    @TableField("discount_rate")
    private BigDecimal discountRate;

    @Schema(description = "最低消费金额（null表示无限制）")
    @TableField("min_amount")
    private BigDecimal minAmount;

    @Schema(description = "最高优惠金额（null表示无限制）")
    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @Schema(description = "会员等级ID（null表示适用于所有会员）")
    @TableField("member_level_id")
    private Long memberLevelId;

    @Schema(description = "开始时间（毫秒时间戳，null表示立即生效）")
    @TableField("start_time")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳，null表示永久有效）")
    @TableField("end_time")
    private Long endTime;

    @Schema(description = "状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "排序（数值越小越靠前）")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

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
