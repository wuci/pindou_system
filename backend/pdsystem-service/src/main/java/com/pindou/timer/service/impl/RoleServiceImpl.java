package com.pindou.timer.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.CreateRoleRequest;
import com.pindou.timer.dto.RoleInfoResponse;
import com.pindou.timer.dto.RoleQueryRequest;
import com.pindou.timer.dto.UpdateRoleRequest;
import com.pindou.timer.entity.Role;
import com.pindou.timer.entity.User;
import com.pindou.timer.mapper.RoleMapper;
import com.pindou.timer.mapper.UserMapper;
import com.pindou.timer.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    /**
     * 超级管理员角色编码
     */
    private static final String SUPER_ADMIN_CODE = "super_admin";

    /**
     * 内置管理员角色编码
     */
    private static final String ADMIN_CODE = "admin";

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public PageResult<RoleInfoResponse> getRoleList(RoleQueryRequest request) {
        log.info("获取角色列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        // 角色名称模糊查询
        if (StringUtils.isNotBlank(request.getName())) {
            wrapper.like(Role::getName, request.getName());
        }

        // 角色编码精确查询
        if (StringUtils.isNotBlank(request.getCode())) {
            wrapper.eq(Role::getCode, request.getCode());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(Role::getStatus, request.getStatus());
        }

        // 按排序字段排序
        wrapper.orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreatedAt);

        // 分页查询
        Page<Role> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Role> resultPage = roleMapper.selectPage(page, wrapper);

        // 转换结果
        List<RoleInfoResponse> records = resultPage.getRecords().stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        PageResult<RoleInfoResponse> pageResult = new PageResult<>();
        pageResult.setList(records);
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPage(request.getPage());
        pageResult.setPageSize(request.getPageSize());

        log.info("获取角色列表成功: total={}", pageResult.getTotal());
        return pageResult;
    }

    @Override
    public List<RoleInfoResponse> getAllRoles() {
        log.info("获取所有角色");

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1)
                .orderByAsc(Role::getSort);

        List<Role> roles = roleMapper.selectList(wrapper);

        List<RoleInfoResponse> responses = roles.stream()
                .map(this::convertToInfoResponse)
                .collect(Collectors.toList());

        log.info("获取所有角色成功: count={}", responses.size());
        return responses;
    }

    @Override
    public RoleInfoResponse getRoleDetail(String roleId) {
        log.info("获取角色详情: roleId={}", roleId);

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        return convertToInfoResponse(role);
    }

    @Override
    public String createRole(CreateRoleRequest request) {
        log.info("创建角色: request={}", request);

        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, request.getCode());
        Role existingRole = roleMapper.selectOne(wrapper);
        if (existingRole != null) {
            throw new BusinessException(ErrorCode.ROLE_NAME_EXISTS, "角色编码已存在");
        }

        // 创建角色
        Role role = new Role();
        role.setId(generateRoleId());
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setSort(request.getSort());
        role.setStatus(request.getStatus());
        role.setDescription(request.getDescription());
        role.setIsBuiltIn(0);

        // 设置权限列表
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            JSONArray jsonArray = JSONUtil.createArray();
            request.getPermissions().forEach(jsonArray::add);
            role.setPermissions(jsonArray.toString());
        } else {
            role.setPermissions("[]");
        }

        roleMapper.insert(role);

        log.info("创建角色成功: roleId={}", role.getId());
        return role.getId();
    }

    @Override
    public Boolean updateRole(UpdateRoleRequest request) {
        log.info("更新角色: request={}", request);

        // 查询角色是否存在
        Role role = roleMapper.selectById(request.getId());
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        // 内置角色不允许修改部分字段
        if (role.getIsBuiltIn() == 1) {
            // 内置角色只允许修改权限列表和状态
            if (request.getPermissions() != null) {
                JSONArray jsonArray = JSONUtil.createArray();
                request.getPermissions().forEach(jsonArray::add);
                role.setPermissions(jsonArray.toString());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
        } else {
            // 非内置角色可以修改所有字段
            if (StringUtils.isNotBlank(request.getName())) {
                role.setName(request.getName());
            }
            if (request.getPermissions() != null) {
                JSONArray jsonArray = JSONUtil.createArray();
                request.getPermissions().forEach(jsonArray::add);
                role.setPermissions(jsonArray.toString());
            }
            if (request.getSort() != null) {
                role.setSort(request.getSort());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
        }

        int result = roleMapper.updateById(role);

        log.info("更新角色成功: roleId={}", request.getId());
        return result > 0;
    }

    @Override
    public Boolean deleteRole(String roleId) {
        log.info("删除角色: roleId={}", roleId);

        // 查询角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        // 内置角色不允许删除
        if (role.getIsBuiltIn() == 1) {
            throw new BusinessException(ErrorCode.ROLE_SYSTEM_BUILTIN, "内置角色不允许删除");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRoleId, roleId);
        Long userCount = userMapper.selectCount(userWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.ROLE_IN_USE, "该角色下还有用户，无法删除");
        }

        int result = roleMapper.deleteById(roleId);

        log.info("删除角色成功: roleId={}", roleId);
        return result > 0;
    }

    @Override
    public List<String> getRolePermissions(String roleId) {
        log.info("获取角色权限列表: roleId={}", roleId);

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "角色不存在");
        }

        // 超级管理员拥有所有权限
        if (SUPER_ADMIN_CODE.equals(role.getCode())) {
            return getAllPermissions();
        }

        // 解析权限列表
        List<String> permissions = new ArrayList<>();
        if (StringUtils.isNotBlank(role.getPermissions())) {
            try {
                JSONArray jsonArray = JSONUtil.parseArray(role.getPermissions());
                permissions = jsonArray.toList(String.class);
            } catch (Exception e) {
                log.warn("解析角色权限失败: roleId={}, permissions={}", roleId, role.getPermissions());
            }
        }

        return permissions;
    }

    @Override
    public Boolean hasPermission(String userId, String permission) {
        log.info("检查用户权限: userId={}, permission={}", userId, permission);

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
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

        // 解析权限列表
        if (StringUtils.isNotBlank(role.getPermissions())) {
            try {
                JSONArray jsonArray = JSONUtil.parseArray(role.getPermissions());
                return jsonArray.contains(permission);
            } catch (Exception e) {
                log.warn("解析角色权限失败: roleId={}, permissions={}", role.getId(), role.getPermissions());
            }
        }

        return false;
    }

    /**
     * 转换为角色信息响应
     */
    private RoleInfoResponse convertToInfoResponse(Role role) {
        RoleInfoResponse response = new RoleInfoResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setCode(role.getCode());
        response.setSort(role.getSort());
        response.setStatus(role.getStatus());
        response.setIsBuiltIn(role.getIsBuiltIn());
        response.setDescription(role.getDescription());
        response.setCreatedAt(role.getCreatedAt());
        response.setUpdatedAt(role.getUpdatedAt());

        // 解析权限列表
        List<String> permissions = new ArrayList<>();
        if (StringUtils.isNotBlank(role.getPermissions())) {
            try {
                JSONArray jsonArray = JSONUtil.parseArray(role.getPermissions());
                permissions = jsonArray.toList(String.class);
            } catch (Exception e) {
                log.warn("解析角色权限失败: roleId={}, permissions={}", role.getId(), role.getPermissions());
            }
        }
        response.setPermissions(permissions);

        return response;
    }

    /**
     * 生成角色ID
     */
    private String generateRoleId() {
        return "ROLE_" + System.currentTimeMillis();
    }

    /**
     * 获取所有权限列表
     */
    private List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();

        // 用户管理权限
        permissions.add("user:view");
        permissions.add("user:create");
        permissions.add("user:update");
        permissions.add("user:delete");
        permissions.add("user:resetPassword");

        // 角色管理权限
        permissions.add("role:view");
        permissions.add("role:create");
        permissions.add("role:update");
        permissions.add("role:delete");

        // 桌台管理权限
        permissions.add("table:view");
        permissions.add("table:create");
        permissions.add("table:update");
        permissions.add("table:delete");

        // 订单管理权限
        permissions.add("order:view");
        permissions.add("order:create");
        permissions.add("order:update");
        permissions.add("order:delete");
        permissions.add("order:export");

        // 配置管理权限
        permissions.add("config:view");
        permissions.add("config:update");

        // 统计报表权限
        permissions.add("statistics:view");

        return permissions;
    }
}
