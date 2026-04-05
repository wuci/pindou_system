package com.pindou.timer.controller;

import com.pindou.timer.annotation.LogOperation;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.*;
import com.pindou.timer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 会员管理控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Tag(name = "会员管理接口", description = "会员管理相关接口")
@RestController
@RequestMapping("/api/members")
public class MemberController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 获取会员列表（分页）
     */
    @Operation(summary = "获取会员列表")
    @GetMapping
    public Result<PageResult<MemberResponse>> getMemberList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        log.info("获取会员列表请求: page={}, pageSize={}, keyword={}", page, pageSize, keyword);

        MemberQueryRequest request = new MemberQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);

        PageResult<MemberResponse> result = memberService.getMemberList(request);

        return Result.success(result);
    }

    /**
     * 搜索会员
     */
    @Operation(summary = "搜索会员")
    @GetMapping("/search")
    public Result<List<MemberResponse>> searchMembers(
            @RequestParam(required = false) String keyword) {

        log.info("搜索会员请求: keyword={}", keyword);

        List<MemberResponse> members = memberService.searchMembers(keyword);

        return Result.success(members);
    }

    /**
     * 获取会员详情
     */
    @Operation(summary = "获取会员详情")
    @GetMapping("/{id}")
    public Result<MemberResponse> getMemberDetail(
            @Parameter(description = "会员ID") @PathVariable("id") Long memberId) {

        log.info("获取会员详情请求: memberId={}", memberId);

        MemberResponse member = memberService.getMemberDetail(memberId);

        return Result.success(member);
    }

    /**
     * 创建会员
     */
    @Operation(summary = "创建会员")
    @LogOperation(
        module = "会员管理",
        operation = "创建会员",
        description = "创建会员【#request.name】，手机号【#request.phone】"
    )
    @PostMapping
    public Result<Long> createMember(@Validated @RequestBody CreateMemberRequest request) {
        log.info("创建会员请求: name={}, phone={}", request.getName(), request.getPhone());

        Long memberId = memberService.createMember(request);

        return Result.success(memberId);
    }

    /**
     * 更新会员
     */
    @Operation(summary = "更新会员")
    @LogOperation(
        module = "会员管理",
        operation = "更新会员",
        description = "更新会员【#memberId】信息"
    )
    @PutMapping("/{id}")
    public Result<Void> updateMember(
            @Parameter(description = "会员ID") @PathVariable("id") Long memberId,
            @Validated @RequestBody UpdateMemberRequest request) {

        log.info("更新会员请求: memberId={}", memberId);

        memberService.updateMember(memberId, request);

        return Result.success();
    }

    /**
     * 删除会员
     */
    @Operation(summary = "删除会员")
    @LogOperation(
        module = "会员管理",
        operation = "删除会员",
        description = "删除会员【#memberId】"
    )
    @DeleteMapping("/{id}")
    public Result<Void> deleteMember(
            @Parameter(description = "会员ID") @PathVariable("id") Long memberId) {

        log.info("删除会员请求: memberId={}", memberId);

        memberService.deleteMember(memberId);

        return Result.success();
    }

    /**
     * 计算会员折扣
     */
    @Operation(summary = "计算会员折扣")
    @PostMapping("/{id}/discount")
    public Result<CalculateDiscountResponse> calculateDiscount(
            @Parameter(description = "会员ID") @PathVariable("id") Long memberId,
            @Validated @RequestBody CalculateDiscountRequest request) {

        log.info("计算会员折扣请求: memberId={}, originalAmount={}", memberId, request.getOriginalAmount());

        CalculateDiscountResponse response = memberService.calculateDiscount(memberId, request);

        return Result.success(response);
    }

    /**
     * 会员充值
     */
    @Operation(summary = "会员充值")
    @LogOperation(
        module = "会员管理",
        operation = "会员充值",
        description = "会员【#memberId】充值【#request.amount】元，支付方式【#request.paymentMethod】"
    )
    @PostMapping("/{id}/recharge")
    public Result<BigDecimal> recharge(
            @Parameter(description = "会员ID") @PathVariable("id") Long memberId,
            @Validated @RequestBody RechargeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("会员充值请求: memberId={}, amount={}", memberId, request.getAmount());

        // 获取操作员信息
        String operatorId = "";
        String operatorName = "system";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                // 这里需要注入UserService来获取用户ID
                // operatorId = userService.getUserIdFromToken(token);
                operatorId = memberId.toString(); // 临时使用会员ID
            } catch (Exception e) {
                // Token无效
            }
        }

        BigDecimal balanceAfter = memberService.recharge(memberId, request, operatorId, operatorName);

        return Result.success(balanceAfter);
    }
}
