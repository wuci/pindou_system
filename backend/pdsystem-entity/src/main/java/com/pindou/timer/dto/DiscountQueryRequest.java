package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 折扣查询请求DTO
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Schema(description = "折扣查询请求")
public class DiscountQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "折扣名称（模糊查询）")
    private String name;

    @Schema(description = "折扣类型：1-固定折扣 2-会员折扣 3-活动折扣")
    private Integer type;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
