package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺规则响应DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "店铺规则响应")
public class DesignStoreRulesResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID")
    private String id;

    @Schema(description = "规则分类：packages-套餐规则, services-增值服务, safety-安全须知, other-其他规定")
    private String category;

    @Schema(description = "规则标题")
    private String title;

    @Schema(description = "规则内容")
    private String content;

    @Schema(description = "规则类型：table-表格数据, list-列表, warning-警告框, special-特色服务")
    private String ruleType;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "更新时间")
    private Long updatedAt;
}
