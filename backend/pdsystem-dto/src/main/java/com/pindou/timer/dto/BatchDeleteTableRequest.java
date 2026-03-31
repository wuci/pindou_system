package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除桌台请求
 *
 * @author wuci
 * @since 2026-03-30
 */
@Data
@Schema(description = "批量删除桌台请求")
public class BatchDeleteTableRequest {

    /**
     * 桌台ID列表
     */
    @NotEmpty(message = "桌台ID列表不能为空")
    @Schema(description = "桌台ID列表", required = true)
    private List<Integer> tableIds;
}
