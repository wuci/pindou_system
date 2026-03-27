package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 桌台配置请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "桌台配置请求")
public class TableConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "桌台数量", required = true, example = "10")
    @NotNull(message = "桌台数量不能为空")
    @Min(value = 1, message = "桌台数量不能小于1")
    @Max(value = 50, message = "桌台数量不能大于50")
    private Integer tableCount;
}
