package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 计费规则配置请求DTO
 *
 * @author wuci
 * @date 2026-03-28
 */
@Data
@Schema(description = "计费规则配置请求")
public class BillingRuleConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道计费规则列表")
    private List<ChannelBillingRuleRequest> channels;
}
