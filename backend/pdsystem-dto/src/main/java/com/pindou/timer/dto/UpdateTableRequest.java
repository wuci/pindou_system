package com.pindou.timer.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新桌台请求DTO
 *
 * @author wuci
 * @date 2026-03-29
 */
@Data
public class UpdateTableRequest implements Serializable {

    /**
     * 桌台ID
     */
    private Integer id;

    /**
     * 桌台名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;
}
