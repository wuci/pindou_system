package com.pindou.timer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 桌台分类请求DTO
 *
 * @author wuci
 * @date 2026-03-29
 */
@Data
public class TableCategoryRequest implements Serializable {

    /**
     * 分类ID（编辑时传入）
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
