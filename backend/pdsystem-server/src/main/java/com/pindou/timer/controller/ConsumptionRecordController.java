package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.service.ConsumptionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消费记录控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Tag(name = "消费记录接口", description = "消费记录相关接口")
@RestController
@RequestMapping("/api/members/{memberId}/consumption-records")
public class ConsumptionRecordController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(ConsumptionRecordController.class);

    private final ConsumptionRecordService consumptionRecordService;

    public ConsumptionRecordController(ConsumptionRecordService consumptionRecordService) {
        this.consumptionRecordService = consumptionRecordService;
    }

    /**
     * 获取会员消费记录
     */
    @Operation(summary = "获取会员消费记录")
    @GetMapping
    public Result<PageResult<Map<String, Object>>> getConsumptionRecords(
            @Parameter(description = "会员ID") @PathVariable("memberId") Long memberId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        log.info("获取消费记录请求: memberId={}, page={}, pageSize={}", memberId, page, pageSize);

        PageResult<Map<String, Object>> result = consumptionRecordService.getConsumptionRecords(memberId, page, pageSize);

        return Result.success(result);
    }
}
