package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 单条计费规则请求DTO
 *
 * @author wuci
 * @date 2026-03-30
 */
@Data
@Schema(description = "单条计费规则")
public class BillingRuleItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "时长（分钟），null表示不限时")
    private Integer minutes;

    @Schema(description = "价格（元），保留两位小数")
    private Double price;

    @Schema(description = "是否不限时")
    private Boolean unlimited;
}
