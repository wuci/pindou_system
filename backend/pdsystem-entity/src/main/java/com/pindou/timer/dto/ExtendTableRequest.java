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

    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合", example = "offline")
    private String paymentMethod;

    @Schema(description = "备注", example = "客户续费")
    private String note;

    @Schema(description = "活动折扣ID", example = "123")
    private String discountId;
}
