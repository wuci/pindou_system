package com.pindou.timer.controller;

import com.pindou.timer.common.result.Result;
import com.pindou.timer.dto.TableLayoutConfigRequest;
import com.pindou.timer.dto.TableLayoutConfigResponse;
import com.pindou.timer.service.TableLayoutConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 桌台布局配置控制器
 *
 * @author wuci
 * @since 2026-03-30
 */
@RestController
@RequestMapping("/api/table-layout-config")
public class TableLayoutConfigController extends ETSBaseController {

    @Resource
    private TableLayoutConfigService layoutConfigService;

    /**
     * 获取布局配置
     *
     * @param categoryId 分类ID，0表示全局默认布局
     * @return 布局配置响应
     */
    @GetMapping("/{categoryId}")
    public Result<TableLayoutConfigResponse> getLayoutConfig(@PathVariable Long categoryId) {
        TableLayoutConfigResponse response = layoutConfigService.getLayoutConfig(categoryId);
        return Result.success(response);
    }

    /**
     * 保存布局配置
     *
     * @param request 布局配置请求
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> saveLayoutConfig(@RequestBody TableLayoutConfigRequest request) {
        layoutConfigService.saveLayoutConfig(request);
        return Result.success();
    }
}
