package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 营收趋势查询请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "营收趋势查询请求")
public class RevenueTrendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "天数（默认7天）")
    private Integer days = 7;
}
