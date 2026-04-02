package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 续费请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "续费请求")
public class ExtendTableRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "额外时长（秒）", example = "3600")
    @NotNull(message = "额外时长不能为空")
    @Min(value = 0, message = "额外时长不能小于0")
    private Integer additionalDuration;

    @Schema(description = "订餐渠道", example = "store")
    private String channel;

    @Schema(description = "备注", example = "客户续费")
    private String note;
}
