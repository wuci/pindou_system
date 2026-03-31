package com.pindou.timer.interceptor;

import cn.hutool.json.JSONUtil;
import com.pindou.timer.annotation.RequirePermission;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.entity.Role;
import com.pindou.timer.entity.User;
import com.pindou.timer.mapper.RoleMapper;
import com.pindou.timer.mapper.UserMapper;
import com.pindou.timer.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 权限验证拦截器
 * 基于 @RequirePermission 注解进行权限验证
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN_CODE = "super_admin";

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // 如果没有权限注解，直接放行
        if (requirePermission == null) {
            // 检查类级别注解
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
            if (requirePermission == null) {
                return true;
            }
        }

        // 获取用户ID
        String userId = getUserIdFromToken(request);
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        // 验证权限
        boolean hasPermission = checkPermission(userId, requirePermission);
        if (!hasPermission) {
            log.warn("权限验证失败: userId={}, requiredPermissions={}",
                    userId, Arrays.toString(requirePermission.value()));
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有操作权限");
        }

        return true;
    }

    /**
     * 从Token中获取用户ID
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isBlank(token)) {
            return null;
        }

        if (token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        }

        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.warn("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查用户权限
     */
    private boolean checkPermission(String userId, RequirePermission requirePermission) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 用户禁用则无权限
        if (user.getStatus() == 0) {
            return false;
        }

        // 查询用户角色
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            return false;
        }

        // 角色禁用则无权限
        if (role.getStatus() == 0) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (SUPER_ADMIN_CODE.equals(role.getCode())) {
            return true;
        }

        // 解析角色权限列表
        List<String> rolePermissions = parsePermissions(role.getPermissions());
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return false;
        }

        // 检查权限
        String[] requiredPermissions = requirePermission.value();
        if (requirePermission.logical() == RequirePermission.Logical.AND) {
            // AND模式：需要拥有所有权限
            return Arrays.stream(requiredPermissions)
                    .allMatch(rolePermissions::contains);
        } else {
            // OR模式：拥有任意一个权限即可
            return Arrays.stream(requiredPermissions)
                    .anyMatch(rolePermissions::contains);
        }
    }

    /**
     * 解析权限列表
     */
    private List<String> parsePermissions(String permissionsStr) {
        if (StringUtils.isBlank(permissionsStr)) {
            return null;
        }

        try {
            return JSONUtil.toList(permissionsStr, String.class);
        } catch (Exception e) {
            log.warn("解析权限列表失败: {}", permissionsStr);
            return null;
        }
    }
}
