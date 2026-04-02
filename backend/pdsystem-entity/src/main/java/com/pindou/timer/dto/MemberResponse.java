package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员信息响应DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "会员信息响应")
public class MemberResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "会员名称")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "累计消费金额")
    private BigDecimal totalAmount;

    @Schema(description = "会员余额")
    private BigDecimal balance;

    @Schema(description = "会员等级ID")
    private Long levelId;

    @Schema(description = "会员等级名称")
    private String levelName;

    @Schema(description = "折扣率")
    private BigDecimal discountRate;

    @Schema(description = "创建时间（毫秒时间戳）")
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    private Long updatedAt;
}
