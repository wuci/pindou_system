package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 渠道计费规则请求DTO
 *
 * @author wuci
 * @date 2026-03-30
 */
@Data
@Schema(description = "渠道计费规则")
public class ChannelBillingRuleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道代码：store-店内，meituan-美团，dianping-大众点评")
    private String channel;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "计费规则列表")
    private List<BillingRuleItemRequest> rules;
}
