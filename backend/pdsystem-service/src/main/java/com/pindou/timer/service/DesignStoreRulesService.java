package com.pindou.timer.service;

import com.pindou.timer.dto.DesignStoreRulesRequest;
import com.pindou.timer.dto.DesignStoreRulesResponse;

import java.util.List;

/**
 * 店铺规则Service接口
 *
 * @author wuci
 * @date 2026-04-06
 */
public interface DesignStoreRulesService {

    /**
     * 根据分类获取规则列表
     *
     * @param category 规则分类（可选，为空则返回所有）
     * @return 规则列表
     */
    List<DesignStoreRulesResponse> getRulesByCategory(String category);

    /**
     * 根据ID获取规则
     *
     * @param id 规则ID
     * @return 规则响应
     */
    DesignStoreRulesResponse getRuleById(String id);

    /**
     * 创建规则
     *
     * @param request 规则请求
     * @return 规则响应
     */
    DesignStoreRulesResponse createRule(DesignStoreRulesRequest request);

    /**
     * 更新规则
     *
     * @param request 规则请求
     * @return 规则响应
     */
    DesignStoreRulesResponse updateRule(DesignStoreRulesRequest request);

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 是否成功
     */
    Boolean deleteRule(String id);

    /**
     * 切换规则启用状态
     *
     * @param id 规则ID
     * @return 是否成功
     */
    Boolean toggleRuleEnabled(String id);
}
