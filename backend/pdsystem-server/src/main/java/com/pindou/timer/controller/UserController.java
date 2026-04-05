package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.*;
import com.pindou.timer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Tag(name = "用户管理接口", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户列表（分页）
     */
    @Operation(summary = "获取用户列表")
    @GetMapping
    public Result<PageResult<UserResponse>> getUserList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) Integer status) {

        log.info("获取用户列表请求: page={}, pageSize={}, username={}, roleId={}, status={}",
                page, pageSize, username, roleId, status);

        UserQueryRequest request = new UserQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setUsername(username);
        request.setRoleId(roleId);
        request.setStatus(status);

        PageResult<UserResponse> result = userService.getUserList(request);

        return Result.success(result);
    }

    /**
     * 获取所有用户列表（不分页）
     */
    @Operation(summary = "获取所有用户列表")
    @GetMapping("/all")
    public Result<List<UserResponse>> getAllUsers() {
        log.info("获取所有用户列表请求");

        List<UserResponse> users = userService.getAllUsers();

        return Result.success(users);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Void> createUser(@Validated @RequestBody CreateUserRequest request) {
        log.info("创建用户请求: username={}", request.getUsername());

        userService.createUser(request);

        return Result.success();
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable("id") String userId,
            @Validated @RequestBody UpdateUserRequest request) {

        log.info("更新用户请求: userId={}", userId);

        userService.updateUser(userId, request);

        return Result.success();
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable("id") String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // 获取当前用户ID
        String operatorId = "";
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                operatorId = userService.getUserIdFromToken(token);
            } catch (Exception e) {
                // Token无效
            }
        }

        log.info("删除用户请求: userId={}, operatorId={}", userId, operatorId);

        userService.deleteUser(userId, operatorId);

        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable("id") String userId,
            @Validated @RequestBody ResetPasswordRequest request) {

        log.info("重置用户密码请求: userId={}", userId);

        userService.resetPassword(userId, request.getPassword());

        return Result.success();
    }
}
