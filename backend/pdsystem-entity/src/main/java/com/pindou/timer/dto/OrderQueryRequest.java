package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单查询请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "订单查询请求")
public class OrderQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码，从1开始")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;

    @Schema(description = "订单状态：active-进行中 completed-已完成")
    private String status;

    @Schema(description = "桌台编号")
    private Integer tableId;

    @Schema(description = "开始时间（毫秒时间戳）")
    private Long startTime;

    @Schema(description = "结束时间（毫秒时间戳）")
    private Long endTime;

    @Schema(description = "关键词（桌台名称/操作员）")
    private String keyword;
}
