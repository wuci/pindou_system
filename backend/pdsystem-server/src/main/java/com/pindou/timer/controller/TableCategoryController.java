package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.TableCategoryRequest;
import com.pindou.timer.dto.TableCategoryResponse;
import com.pindou.timer.service.TableCategoryService;
import com.pindou.timer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 桌台分类控制器
 *
 * @author wuci
 * @date 2026-03-29
 */
@RestController
@RequestMapping("/api/table-category")
@Tag(name = "桌台分类", description = "桌台分类管理接口")
public class TableCategoryController extends ETSBaseController {

    private static final Logger log = LoggerFactory.getLogger(TableCategoryController.class);

    @Resource
    private TableCategoryService categoryService;

    @Resource
    private UserService userService;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有分类")
    public Result<List<TableCategoryResponse>> list() {
        List<TableCategoryResponse> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 创建分类
     */
    @PostMapping("/create")
    @Operation(summary = "创建分类")
    public Result<Long> create(
            @RequestBody TableCategoryRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsername(authHeader);
        log.info("创建桌台分类, operator={}, name={}", username, request.getName());
        Long id = categoryService.createCategory(request);
        return Result.success(id);
    }

    /**
     * 更新分类
     */
    @PostMapping("/update")
    @Operation(summary = "更新分类")
    public Result<Void> update(
            @RequestBody TableCategoryRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsername(authHeader);
        log.info("更新桌台分类, operator={}, id={}", username, request.getId());
        categoryService.updateCategory(request);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsername(authHeader);
        log.info("删除桌台分类, operator={}, id={}", username, id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情")
    public Result<TableCategoryResponse> get(@PathVariable Long id) {
        TableCategoryResponse category = categoryService.getCategory(id);
        return Result.success(category);
    }

    /**
     * 从 Authorization header 获取用户名
     */
    private String getUsername(String authHeader) {
        if (authHeader != null && authHeader.length() > 7) {
            try {
                String token = authHeader.substring(7);
                return userService.getUserIdFromToken(token);
            } catch (Exception e) {
                // Token无效
            }
        }
        return "system";
    }
}
