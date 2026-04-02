package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新会员等级请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "更新会员等级请求")
public class UpdateMemberLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "等级名称")
    @NotBlank(message = "等级名称不能为空")
    private String name;

    @Schema(description = "最小累计金额")
    private BigDecimal minAmount;

    @Schema(description = "最大累计金额")
    private BigDecimal maxAmount;

    @Schema(description = "折扣率")
    @DecimalMin(value = "0.1", message = "折扣率不能小于0.1")
    @DecimalMax(value = "1.0", message = "折扣率不能大于1.0")
    private BigDecimal discountRate;

    @Schema(description = "排序")
    private Integer sort;
}
