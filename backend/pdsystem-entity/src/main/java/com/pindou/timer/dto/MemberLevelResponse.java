package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "会员等级响应")
public class MemberLevelResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "等级ID")
    private Long id;

    @Schema(description = "等级名称")
    private String name;

    @Schema(description = "最小累计金额")
    private BigDecimal minAmount;

    @Schema(description = "最大累计金额")
    private BigDecimal maxAmount;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;
}
