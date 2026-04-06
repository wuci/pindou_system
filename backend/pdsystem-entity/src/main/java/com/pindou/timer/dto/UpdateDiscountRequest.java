package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新折扣请求DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "更新折扣请求")
public class UpdateDiscountRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "折扣名称")
    @NotBlank(message = "折扣名称不能为空")
    @Size(max = 50, message = "折扣名称长度不能超过50个字符")
    private String name;

    @Schema(description = "折扣类型：1-固定折扣 2-会员折扣 3-活动折扣")
    @NotNull(message = "折扣类型不能为空")
    @Min(value = 1, message = "折扣类型值无效")
    @Max(value = 3, message = "折扣类型值无效")
    private Integer type;

    @Schema(description = "折扣率（0.9表示9折，1.0表示不打折）")
    @NotNull(message = "折扣率不能为空")
    @DecimalMin(value = "0.1", message = "折扣率不能小于0.1")
    @DecimalMax(value = "1.0", message = "折扣率不能大于1.0")
    private BigDecimal discountRate;

    @Schema(description = "最低消费金额")
    @DecimalMin(value = "0", message = "最低消费金额不能为负数")
    private BigDecimal minAmount;

    @Schema(description = "最高优惠金额")
    @DecimalMin(value = "0", message = "最高优惠金额不能为负数")
    private BigDecimal maxDiscount;

    @Schema(description = "会员等级ID")
    private Long memberLevelId;

    @Schema(description = "开始时间（毫秒时间戳）")
    @Min(value = 0, message = "开始时间无效")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    @Min(value = 0, message = "结束时间无效")
    private Long endTime;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sort;

    @Schema(description = "描述")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}
