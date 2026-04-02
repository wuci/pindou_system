package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.RechargeRecordResponse;
import com.pindou.timer.service.RechargeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 充值记录控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "充值记录接口", description = "充值记录相关接口")
@RestController
@RequestMapping("/members/{memberId}/recharge-records")
public class RechargeRecordController {

    private final RechargeRecordService rechargeRecordService;

    public RechargeRecordController(RechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
    }

    /**
     * 获取会员充值记录
     */
    @Operation(summary = "获取会员充值记录")
    @GetMapping
    public Result<PageResult<RechargeRecordResponse>> getRechargeRecords(
            @Parameter(description = "会员ID") @PathVariable("memberId") Long memberId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        log.info("获取充值记录请求: memberId={}, page={}, pageSize={}", memberId, page, pageSize);

        PageResult<RechargeRecordResponse> result = rechargeRecordService.getRechargeRecords(memberId, page, pageSize);

        return Result.success(result);
    }
}
