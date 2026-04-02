package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 结账请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "结账请求")
public class EndTableRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID（可选，选择会员后应用折扣）")
    private Long memberId;

    @Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合", example = "offline")
    private String paymentMethod;
}
