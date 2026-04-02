package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 系统参数配置请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "系统参数配置请求")
public class SystemConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "延长时间（分钟）", required = true, example = "30")
    @NotNull(message = "延长时间不能为空")
    @Min(value = 1, message = "延长时间不能小于1分钟")
    @Max(value = 120, message = "延长时间不能大于120分钟")
    private Integer extendTime;

    @Schema(description = "无效订单时间（分钟）", example = "3")
    @Min(value = 0, message = "无效订单时间不能小于0分钟")
    @Max(value = 60, message = "无效订单时间不能大于60分钟")
    private Integer invalidOrderTime;
}
