package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置响应DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "系统配置响应")
public class ConfigResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值（JSON字符串）")
    private String configValue;

    @Schema(description = "配置描述")
    private String description;
}
