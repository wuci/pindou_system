package com.pindou.timer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.User;

import java.util.List;

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

    /**
     * 获取用户列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<UserResponse> getUserList(UserQueryRequest request);

    /**
     * 获取所有用户列表（不分页，用于下拉选择）
     *
     * @return 用户列表
     */
    List<UserResponse> getAllUsers();

    /**
     * 创建用户
     *
     * @param request 创建请求
     */
    void createUser(CreateUserRequest request);

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateUser(String userId, UpdateUserRequest request);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @param operatorId 操作用户ID
     */
    void deleteUser(String userId, String operatorId);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(String userId, String newPassword);
}
