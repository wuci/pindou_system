package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 开始计时请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "开始计时请求")
public class StartTimerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "预设时长（秒），0表示不设时长", example = "3600")
    @NotNull(message = "预设时长不能为空")
    @Min(value = 0, message = "预设时长不能小于0")
    private Integer presetDuration;

    @Schema(description = "是否为不限时套餐", example = "false")
    private Boolean unlimited;

    @Schema(description = "订餐渠道", example = "store")
    private String channel;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "备注", example = "会员客户")
    private String note;

    @Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合", example = "offline")
    private String paymentMethod;

    @Schema(description = "活动折扣ID", example = "123")
    private String discountId;
}
