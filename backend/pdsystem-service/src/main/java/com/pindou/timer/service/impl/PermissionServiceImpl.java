package com.pindou.timer.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.dto.CreatePermissionRequest;
import com.pindou.timer.dto.PermissionResponse;
import com.pindou.timer.dto.UpdatePermissionRequest;
import com.pindou.timer.entity.Permission;
import com.pindou.timer.common.exception.PDException;
import com.pindou.timer.mapper.PermissionMapper;
import com.pindou.timer.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限配置Service实现
 *
 * @author wuci
 * @date 2026-04-03
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<PermissionResponse> getPermissionTree() {
        // 查询所有启用的权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getSort);

        List<Permission> allPermissions = permissionMapper.selectList(wrapper);

        // 转换为Response并构建树形结构
        List<PermissionResponse> responseList = allPermissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return buildTree(responseList, "0");
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
                .orderByAsc(Permission::getSort);

        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionDetail(String permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw PDException.permissionNotFound();
        }
        return convertToResponse(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPermission(CreatePermissionRequest request) {
        // 检查权限编码是否已存在
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermissionKey, request.getPermissionKey());
        Long count = permissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw PDException.permissionKeyExists(request.getPermissionKey());
        }

        Permission permission = new Permission();
        permission.setId(UUID.randomUUID().toString().replace("-", ""));
        permission.setParentId(request.getParentId());
        permission.setPermissionKey(request.getPermissionKey());
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionType(request.getPermissionType());
        permission.setIcon(request.getIcon());
        permission.setPath(request.getPath());
        permission.setSort(request.getSort());
        permission.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        permission.setIsBuiltIn(0);
        permission.setDescription(request.getDescription());

        permissionMapper.insert(permission);
        log.info("创建权限成功：permissionKey={}, permissionName={}",
                request.getPermissionKey(), request.getPermissionName());

        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermission(UpdatePermissionRequest request) {
        Permission permission = permissionMapper.selectById(request.getId());
        if (permission == null) {
            throw PDException.permissionNotFound();
        }

        // 内置权限不允许修改关键信息
        if (permission.getIsBuiltIn() == 1) {
            throw PDException.builtInPermissionNotModifiable();
        }

        permission.setPermissionName(request.getPermissionName());
        permission.setIcon(request.getIcon());
        permission.setPath(request.getPath());
        permission.setSort(request.getSort());
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }
        permission.setDescription(request.getDescription());

        int result = permissionMapper.updateById(permission);
        log.info("更新权限成功：id={}, permissionName={}", request.getId(), request.getPermissionName());

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePermission(String permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw PDException.permissionNotFound();
        }

        // 内置权限不允许删除
        if (permission.getIsBuiltIn() == 1) {
            throw PDException.builtInPermissionNotDeletable();
        }

        // 检查是否有子权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getParentId, permissionId);
        Long childCount = permissionMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw PDException.hasChildPermissions();
        }

        int result = permissionMapper.deleteById(permissionId);
        log.info("删除权限成功：id={}, permissionKey={}", permissionId, permission.getPermissionKey());

        return result > 0;
    }

    /**
     * 构建权限树
     */
    private List<PermissionResponse> buildTree(List<PermissionResponse> allPermissions, String parentId) {
        List<PermissionResponse> result = new ArrayList<>();

        for (PermissionResponse permission : allPermissions) {
            if (parentId.equals(permission.getParentId())) {
                // 递归查找子节点
                List<PermissionResponse> children = buildTree(allPermissions, permission.getId());
                if (!children.isEmpty()) {
                    permission.setChildren(children);
                }
                result.add(permission);
            }
        }

        return result;
    }

    /**
     * 实体转换为Response
     */
    private PermissionResponse convertToResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        BeanUtils.copyProperties(permission, response);
        return response;
    }
}
