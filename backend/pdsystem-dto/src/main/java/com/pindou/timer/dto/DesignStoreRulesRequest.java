package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 店铺规则请求DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "店铺规则请求")
public class DesignStoreRulesRequest {

    @Schema(description = "规则ID（更新时必填）")
    private String id;

    @NotBlank(message = "规则分类不能为空")
    @Schema(description = "规则分类：packages-套餐规则, services-增值服务, safety-安全须知, other-其他规定")
    private String category;

    @Schema(description = "规则标题")
    private String title;

    @NotBlank(message = "规则内容不能为空")
    @Schema(description = "规则内容（HTML格式或JSON数组）")
    private String content;

    @Schema(description = "规则类型：table-表格数据, list-列表, warning-警告框, special-特色服务")
    private String ruleType;

    @NotNull(message = "排序号不能为空")
    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用：0-禁用，1-启用")
    private Boolean isEnabled;
}
