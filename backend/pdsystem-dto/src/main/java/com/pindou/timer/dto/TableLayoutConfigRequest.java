package com.pindou.timer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 桌台布局配置请求DTO
 *
 * @author wuci
 * @since 2026-03-30
 */
@Data
public class TableLayoutConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID，0表示全局默认布局
     */
    private Long categoryId;

    /**
     * 布局配置JSON
     */
    private String config;
}
