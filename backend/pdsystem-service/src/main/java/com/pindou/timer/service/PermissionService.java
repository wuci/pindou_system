package com.pindou.timer.service;

import com.pindou.timer.dto.CreatePermissionRequest;
import com.pindou.timer.dto.PermissionResponse;
import com.pindou.timer.dto.UpdatePermissionRequest;

import java.util.List;

/**
 * 权限配置Service接口
 *
 * @author wuci
 * @date 2026-04-03
 */
public interface PermissionService {

    /**
     * 获取权限树（所有启用的权限）
     *
     * @return 权限树
     */
    List<PermissionResponse> getPermissionTree();

    /**
     * 获取所有权限列表（平铺，不分页）
     *
     * @return 权限列表
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限详情
     */
    PermissionResponse getPermissionDetail(String permissionId);

    /**
     * 创建权限
     *
     * @param request 创建请求
     * @return 权限ID
     */
    String createPermission(CreatePermissionRequest request);

    /**
     * 更新权限
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updatePermission(UpdatePermissionRequest request);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 是否成功
     */
    Boolean deletePermission(String permissionId);
}
