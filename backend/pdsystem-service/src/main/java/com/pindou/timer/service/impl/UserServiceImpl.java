package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Role;
import com.pindou.timer.entity.User;
import com.pindou.timer.mapper.RoleMapper;
import com.pindou.timer.mapper.UserMapper;
import com.pindou.timer.service.UserService;
import com.pindou.timer.util.JwtUtil;
import com.pindou.timer.util.PasswordUtil;
import com.pindou.timer.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final RoleMapper roleMapper;

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60; // 7天（秒）

    public UserServiceImpl(JwtUtil jwtUtil, RedisUtil redisUtil, RoleMapper roleMapper) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.roleMapper = roleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String ip) {
        try {
            log.info("用户登录请求: username={}, ip={}", request.getUsername(), ip);

            // 1. 查询用户
            User user = getByUsername(request.getUsername());
            if (user == null) {
                log.warn("用户不存在: username={}", request.getUsername());
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.USER_NOT_FOUND);
            }
            log.info("找到用户: userId={}, username={}", user.getId(), user.getUsername());

            // 2. 验证状态
            if (user.getStatus() == 0) {
                log.warn("用户已禁用: userId={}", user.getId());
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.USER_DISABLED);
            }

            // 3. 验证密码
            if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
                log.warn("密码错误: userId={}", user.getId());
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.PASSWORD_ERROR);
            }
            log.info("密码验证成功");

            // 4. 查询角色
            Role role = roleMapper.selectById(user.getRoleId());
            if (role == null || role.getStatus() == 0) {
                log.warn("角色无效: roleId={}", user.getRoleId());
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.USER_DISABLED);
            }
            log.info("角色信息: roleId={}, roleName={}", role.getId(), role.getName());

            // 5. 生成Token
            String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername());
            log.info("Token生成成功");

            // 6. 单点登录：清除旧Token
            String oldTokenKey = TOKEN_PREFIX + user.getId();
            try {
                if (redisUtil.hasKey(oldTokenKey)) {
                    redisUtil.delete(oldTokenKey);
                    log.info("清除旧Token: userId={}", user.getId());
                }

                // 7. 存储Token到Redis
                redisUtil.set(oldTokenKey, token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
                log.info("Token存储到Redis成功");
            } catch (Exception e) {
                log.error("Redis操作失败: {}", e.getMessage(), e);
                // Redis失败不影响登录，只是无法实现单点登录
            }

            // 8. 更新最后登录信息
            user.setLastLoginAt(System.currentTimeMillis());
            user.setLastLoginIp(ip);
            updateById(user);
            log.info("更新登录信息成功");

            // 9. 构建用户信息
            UserInfo userInfo = buildUserInfo(user, role);
            log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

            return new LoginResponse(token, userInfo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        try {
            // 从Token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);
            if (StrUtil.isNotBlank(userId)) {
                // 删除Redis中的Token
                String tokenKey = TOKEN_PREFIX + userId;
                redisUtil.delete(tokenKey);
            }
        } catch (Exception e) {
            // Token无效，忽略
        }
    }

    @Override
    public UserInfo getUserInfo(String userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.USER_NOT_FOUND);
        }

        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            role = new Role();
        }

        return buildUserInfo(user, role);
    }

    @Override
    public User getByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 1. 验证Token格式和签名
            if (StrUtil.isBlank(token) || !jwtUtil.validateToken(token)) {
                log.warn("Token格式无效或已过期");
                return false;
            }

            // 2. 从Token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);

            // 3. 尝试验证Redis中的Token（单点登录）
            String tokenKey = TOKEN_PREFIX + userId;
            try {
                String cachedToken = (String) redisUtil.get(tokenKey);
                boolean isValid = token.equals(cachedToken);

                if (!isValid) {
                    log.warn("Redis中的Token不匹配: userId={}", userId);
                }

                return isValid;
            } catch (Exception e) {
                // Redis连接失败时降级处理：只验证JWT本身
                log.warn("Redis连接失败，降级为仅验证JWT: {}", e.getMessage());
                return true;
            }
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.UNAUTHORIZED);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 构建用户信息
     */
    private UserInfo buildUserInfo(User user, Role role) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setRoleId(user.getRoleId());
        userInfo.setRoleName(role.getName());
        userInfo.setLastLoginAt(user.getLastLoginAt());
        userInfo.setLastLoginIp(user.getLastLoginIp());
        userInfo.setCreatedAt(user.getCreatedAt());

        // 解析权限
        if (StrUtil.isNotBlank(role.getPermissions())) {
            try {
                List<String> permissions = JSONUtil.toList(role.getPermissions(), String.class);
                userInfo.setPermissions(permissions);
            } catch (Exception e) {
                log.error("解析权限失败: permissions={}", role.getPermissions(), e);
                userInfo.setPermissions(java.util.Collections.emptyList());
            }
        }

        return userInfo;
    }

    @Override
    public PageResult<UserResponse> getUserList(UserQueryRequest request) {
        log.info("获取用户列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.isNotBlank(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }

        // 角色筛选
        if (StringUtils.isNotBlank(request.getRoleId())) {
            wrapper.eq(User::getRoleId, request.getRoleId());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(User::getStatus, request.getStatus());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreatedAt);

        // 分页查询
        Page<User> page = new Page<>(request.getPage(), request.getPageSize());
        Page<User> resultPage = page(page);

        // 转换结果
        List<UserResponse> records = resultPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageResult<UserResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取用户列表成功: total={}", pageResult.getTotal());
        return pageResult;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("获取所有用户列表");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1) // 只查询启用的用户
                .orderByAsc(User::getCreatedAt);

        List<User> users = list(wrapper);

        return users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserRequest request) {
        log.info("创建用户: username={}", request.getUsername());

        // 验证用户名唯一性
        User existUser = getByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 验证角色存在
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        // 创建用户
        User user = new User();
        user.setId(IdUtil.simpleUUID());
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRoleId(request.getRoleId());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        save(user);

        log.info("创建用户成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(String userId, UpdateUserRequest request) {
        log.info("更新用户: userId={}", userId);

        // 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证角色存在
        if (StringUtils.isNotBlank(request.getRoleId())) {
            Role role = roleMapper.selectById(request.getRoleId());
            if (role == null) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
            }
            user.setRoleId(request.getRoleId());
        }

        // 更新字段
        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user.setUpdatedAt(System.currentTimeMillis());
        updateById(user);

        log.info("更新用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userId, String operatorId) {
        log.info("删除用户: userId={}, operatorId={}", userId, operatorId);

        // 不能删除自己
        if (userId.equals(operatorId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能删除自己");
        }

        // 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 软删除
        removeById(userId);

        log.info("删除用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId, String newPassword) {
        log.info("重置用户密码: userId={}", userId);

        // 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 重置密码
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setUpdatedAt(System.currentTimeMillis());
        updateById(user);

        log.info("重置用户密码成功: userId={}", userId);
    }

    /**
     * 转换为响应DTO
     */
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRoleId(user.getRoleId());
        response.setStatus(user.getStatus());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setLastLoginIp(user.getLastLoginIp());
        response.setCreatedAt(user.getCreatedAt());

        // 查询角色名称
        if (StringUtils.isNotBlank(user.getRoleId())) {
            Role role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                response.setRoleName(role.getName());
            }
        }

        return response;
    }
}
