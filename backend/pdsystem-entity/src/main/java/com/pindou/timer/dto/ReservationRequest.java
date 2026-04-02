package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 桌台预定请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "桌台预定请求")
public class ReservationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预定截止时间（毫秒时间戳）")
    @NotNull(message = "预定截止时间不能为空")
    private Long reservationEndTime;

    @Schema(description = "预订人姓名")
    @NotBlank(message = "预订人姓名不能为空")
    private String reservationName;

    @Schema(description = "预订人手机号")
    @NotBlank(message = "预订人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String reservationPhone;
}
