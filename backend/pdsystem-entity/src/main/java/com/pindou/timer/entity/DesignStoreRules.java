package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pindou.timer.constants.RuleCategoryEnum;
import com.pindou.timer.constants.RuleTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺规则实体类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@TableName("design_store_rules")
@Schema(description = "店铺规则")
public class DesignStoreRules implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "规则ID（UUID）")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String id;

    @Schema(description = "规则分类：packages-套餐规则, services-增值服务, safety-安全须知, other-其他规定")
    @TableField("category")
    private String category;

    @Schema(description = "规则标题")
    @TableField("title")
    private String title;

    @Schema(description = "规则内容（HTML格式，支持富文本）")
    @TableField("content")
    private String content;

    @Schema(description = "规则类型：table-表格数据, list-列表, warning-警告框, special-特色服务")
    @TableField("rule_type")
    private String ruleType;

    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "是否启用：0-禁用，1-启用")
    @TableField("is_enabled")
    private Boolean isEnabled;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField("created_at")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField("updated_at")
    private Long updatedAt;

    @Schema(description = "创建人ID")
    @TableField("created_by")
    private String createdBy;

    @Schema(description = "更新人ID")
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 获取规则分类枚举
     *
     * @return 规则分类枚举
     */
    public RuleCategoryEnum getCategoryEnum() {
        return RuleCategoryEnum.fromCode(this.category);
    }

    /**
     * 获取规则类型枚举
     *
     * @return 规则类型枚举
     */
    public RuleTypeEnum getRuleTypeEnum() {
        return RuleTypeEnum.fromCode(this.ruleType);
    }

    /**
     * 设置规则分类（通过枚举）
     *
     * @param categoryEnum 规则分类枚举
     */
    public void setCategoryEnum(RuleCategoryEnum categoryEnum) {
        this.category = categoryEnum != null ? categoryEnum.getCode() : null;
    }

    /**
     * 设置规则类型（通过枚举）
     *
     * @param ruleTypeEnum 规则类型枚举
     */
    public void setRuleTypeEnum(RuleTypeEnum ruleTypeEnum) {
        this.ruleType = ruleTypeEnum != null ? ruleTypeEnum.getCode() : null;
    }
}
