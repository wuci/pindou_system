package com.pindou.timer.service;

import com.pindou.timer.dto.TableLayoutConfigRequest;
import com.pindou.timer.dto.TableLayoutConfigResponse;

/**
 * 桌台布局配置服务接口
 *
 * @author wuci
 * @since 2026-03-30
 */
public interface TableLayoutConfigService {

    /**
     * 获取布局配置
     *
     * @param categoryId 分类ID，0表示全局默认布局
     * @return 布局配置响应
     */
    TableLayoutConfigResponse getLayoutConfig(Long categoryId);

    /**
     * 保存布局配置
     *
     * @param request 布局配置请求
     */
    void saveLayoutConfig(TableLayoutConfigRequest request);
}
