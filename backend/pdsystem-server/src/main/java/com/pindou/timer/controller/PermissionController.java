package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.CreatePermissionRequest;
import com.pindou.timer.dto.PermissionResponse;
import com.pindou.timer.dto.UpdatePermissionRequest;
import com.pindou.timer.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 权限配置控制器
 *
 * @author wuci
 * @date 2026-04-03
 */
@Slf4j
@Tag(name = "权限配置接口", description = "权限配置管理相关接口")
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 获取权限树
     */
    @Operation(summary = "获取权限树")
    @GetMapping("/tree")
    public Result<List<PermissionResponse>> getPermissionTree() {
        log.info("获取权限树请求");

        List<PermissionResponse> tree = permissionService.getPermissionTree();

        return Result.success(tree);
    }

    /**
     * 获取所有权限列表（平铺）
     */
    @Operation(summary = "获取所有权限")
    @GetMapping("/all")
    public Result<List<PermissionResponse>> getAllPermissions() {
        log.info("获取所有权限请求");

        List<PermissionResponse> permissions = permissionService.getAllPermissions();

        return Result.success(permissions);
    }

    /**
     * 获取权限详情
     */
    @Operation(summary = "获取权限详情")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getPermissionDetail(
            @Parameter(description = "权限ID") @PathVariable("id") String permissionId) {

        log.info("获取权限详情请求: permissionId={}", permissionId);

        PermissionResponse response = permissionService.getPermissionDetail(permissionId);

        return Result.success(response);
    }

    /**
     * 创建权限
     */
    @Operation(summary = "创建权限")
    @PostMapping
    public Result<String> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        log.info("创建权限请求: request={}", request);

        String permissionId = permissionService.createPermission(request);

        return Result.success(permissionId);
    }

    /**
     * 更新权限
     */
    @Operation(summary = "更新权限")
    @PutMapping
    public Result<Boolean> updatePermission(@Valid @RequestBody UpdatePermissionRequest request) {
        log.info("更新权限请求: request={}", request);

        Boolean result = permissionService.updatePermission(request);

        return Result.success(result);
    }

    /**
     * 删除权限
     */
    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePermission(
            @Parameter(description = "权限ID") @PathVariable("id") String permissionId) {

        log.info("删除权限请求: permissionId={}", permissionId);

        Boolean result = permissionService.deletePermission(permissionId);

        return Result.success(result);
    }
}
