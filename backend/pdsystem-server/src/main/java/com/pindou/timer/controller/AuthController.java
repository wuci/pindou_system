package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.LoginRequest;
import com.pindou.timer.dto.LoginResponse;
import com.pindou.timer.dto.UserInfo;
import com.pindou.timer.service.UserService;
import com.pindou.timer.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "认证接口", description = "用户登录、登出、Token验证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Validated @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 获取客户端IP
        String ip = IpUtil.getClientIp(httpRequest);

        log.info("用户登录请求: username={}, ip={}", request.getUsername(), ip);

        // 调用登录服务
        LoginResponse response = userService.login(request, ip);

        log.info("用户登录成功: username={}, userId={}", request.getUsername(), response.getUserInfo().getId());

        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // 提取Token
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

        log.info("用户登出请求");

        // 调用登出服务
        userService.logout(token);

        log.info("用户登出成功");

        return Result.success();
    }

    /**
     * 验证Token
     */
    @Operation(summary = "验证Token")
    @GetMapping("/check")
    public Result<Boolean> checkToken(@RequestHeader("Authorization") String authHeader) {
        // 提取Token
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

        boolean valid = userService.validateToken(token);

        return Result.success(valid);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<UserInfo> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        // 提取Token
        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

        // 从Token中获取用户ID
        String userId = userService.getUserIdFromToken(token);

        // 查询用户信息
        UserInfo userInfo = userService.getUserInfo(userId);

        return Result.success(userInfo);
    }
}
