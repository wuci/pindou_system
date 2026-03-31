package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 提醒配置请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "提醒配置请求")
public class RemindConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提醒阈值（秒）")
    private Integer threshold;

    @Schema(description = "声音开关：0-关闭，1-开启")
    private Integer soundEnabled;

    @Schema(description = "重复间隔（秒）")
    private Integer repeatInterval;
}
