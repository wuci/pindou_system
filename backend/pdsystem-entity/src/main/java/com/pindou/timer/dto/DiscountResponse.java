package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 折扣响应DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "折扣响应")
public class DiscountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "折扣ID")
    private String id;

    @Schema(description = "折扣名称")
    private String name;

    @Schema(description = "折扣类型：1-固定折扣 2-会员折扣 3-活动折扣")
    private Integer type;

    @Schema(description = "折扣类型名称")
    private String typeName;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "最低消费金额")
    private BigDecimal minAmount;

    @Schema(description = "最高优惠金额")
    private BigDecimal maxDiscount;

    @Schema(description = "会员等级ID")
    private Long memberLevelId;

    @Schema(description = "会员等级名称")
    private String memberLevelName;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;
}
