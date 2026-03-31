package com.pindou.timer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 桌台布局配置响应DTO
 *
 * @author wuci
 * @since 2026-03-30
 */
@Data
public class TableLayoutConfigResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 布局配置JSON
     */
    private String config;
}
