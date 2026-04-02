package com.pindou.timer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员查询请求DTO
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@Schema(description = "会员查询请求")
public class MemberQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码（从1开始）")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键词（手机号或姓名）")
    private String keyword;
}
