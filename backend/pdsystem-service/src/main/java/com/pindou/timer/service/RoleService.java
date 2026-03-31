package com.pindou.timer.service;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.dto.CreateRoleRequest;
import com.pindou.timer.dto.RoleInfoResponse;
import com.pindou.timer.dto.RoleQueryRequest;
import com.pindou.timer.dto.UpdateRoleRequest;

import java.util.List;

/**
 * 角色Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface RoleService {

    /**
     * 获取角色列表（分页）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<RoleInfoResponse> getRoleList(RoleQueryRequest request);

    /**
     * 获取所有角色（不分页，用于下拉选择）
     *
     * @return 角色列表
     */
    List<RoleInfoResponse> getAllRoles();

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleInfoResponse getRoleDetail(String roleId);

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色ID
     */
    String createRole(CreateRoleRequest request);

    /**
     * 更新角色
     *
     * @param request 更新请求
     * @return 是否成功
     */
    Boolean updateRole(UpdateRoleRequest request);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteRole(String roleId);

    /**
     * 获取角色权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> getRolePermissions(String roleId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId    用户ID
     * @param permission 权限代码
     * @return 是否拥有权限
     */
    Boolean hasPermission(String userId, String permission);
}
