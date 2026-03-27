package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.dto.LoginRequest;
import com.pindou.timer.dto.LoginResponse;
import com.pindou.timer.dto.UserInfo;
import com.pindou.timer.entity.User;

/**
 * 用户Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      登录IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 用户登出
     *
     * @param token Token
     */
    void logout(String token);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID（UUID）
     * @return 用户信息
     */
    UserInfo getUserInfo(String userId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User getByUsername(String username);

    /**
     * 验证Token
     *
     * @param token Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从Token中获取用户ID
     *
     * @param token Token
     * @return 用户ID（UUID）
     */
    String getUserIdFromToken(String token);
}
