package com.pindou.timer.controller;

import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.CreateRoleRequest;
import com.pindou.timer.dto.RoleInfoResponse;
import com.pindou.timer.dto.RoleQueryRequest;
import com.pindou.timer.dto.UpdateRoleRequest;
import com.pindou.timer.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 角色控制器
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Tag(name = "角色接口", description = "角色管理相关接口")
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 获取角色列表（分页）
     */
    @Operation(summary = "获取角色列表")
    @GetMapping
    public Result<PageResult<RoleInfoResponse>> getRoleList(
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @Parameter(description = "角色名称") @RequestParam(required = false) String name,
            @Parameter(description = "角色编码") @RequestParam(required = false) String code,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        log.info("获取角色列表请求: page={}, pageSize={}, name={}, code={}, status={}",
                page, pageSize, name, code, status);

        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setName(name);
        request.setCode(code);
        request.setStatus(status);

        PageResult<RoleInfoResponse> result = roleService.getRoleList(request);

        return Result.success(result);
    }

    /**
     * 获取所有角色（不分页，用于下拉选择）
     */
    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public Result<List<RoleInfoResponse>> getAllRoles() {
        log.info("获取所有角色请求");

        List<RoleInfoResponse> roles = roleService.getAllRoles();

        return Result.success(roles);
    }

    /**
     * 获取角色详情
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleInfoResponse> getRoleDetail(
            @Parameter(description = "角色ID") @PathVariable("id") String roleId) {

        log.info("获取角色详情请求: roleId={}", roleId);

        RoleInfoResponse response = roleService.getRoleDetail(roleId);

        return Result.success(response);
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色")
    @PostMapping
    public Result<String> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("创建角色请求: request={}", request);

        String roleId = roleService.createRole(request);

        return Result.success(roleId);
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色")
    @PutMapping
    public Result<Boolean> updateRole(@Valid @RequestBody UpdateRoleRequest request) {
        log.info("更新角色请求: request={}", request);

        Boolean result = roleService.updateRole(request);

        return Result.success(result);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(
            @Parameter(description = "角色ID") @PathVariable("id") String roleId) {

        log.info("删除角色请求: roleId={}", roleId);

        Boolean result = roleService.deleteRole(roleId);

        return Result.success(result);
    }

    /**
     * 获取角色权限列表
     */
    @Operation(summary = "获取角色权限列表")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable("id") String roleId) {

        log.info("获取角色权限列表请求: roleId={}", roleId);

        List<String> permissions = roleService.getRolePermissions(roleId);

        return Result.success(permissions);
    }
}
