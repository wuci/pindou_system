package com.pindou.timer.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台分类响应DTO
 *
 * @author wuci
 * @date 2026-03-29
 */
@Data
public class TableCategoryResponse implements Serializable {

    /**
     * 分类ID
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
     * 桌台数量
     */
    private Integer tableCount;

    /**
     * 使用中桌台数量
     */
    private Integer usingCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 备注
     */
    private String remark;
}
