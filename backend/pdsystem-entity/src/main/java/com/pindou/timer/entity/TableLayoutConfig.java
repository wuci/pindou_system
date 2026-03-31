package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台布局配置实体
 *
 * @author wuci
 * @since 2026-03-30
 */
@Data
@TableName("biz_table_layout_config")
public class TableLayoutConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类ID，0表示全局默认布局
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 布局配置JSON
     */
    @TableField("config")
    private String config;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
