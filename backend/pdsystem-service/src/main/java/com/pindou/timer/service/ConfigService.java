package com.pindou.timer.service;

import com.pindou.timer.dto.BillingRuleConfigRequest;
import com.pindou.timer.dto.RemindConfigRequest;
import com.pindou.timer.dto.SystemConfigRequest;

import java.util.Map;

/**
 * 配置Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface ConfigService {

    /**
     * 获取所有配置
     *
     * @return 配置Map (key -> value JSON)
     */
    Map<String, String> getAllConfigs();

    /**
     * 获取计费规则配置
     *
     * @return 计费规则JSON
     */
    String getBillingRuleConfig();

    /**
     * 更新计费规则配置
     *
     * @param request 配置请求
     * @return 是否成功
     */
    Boolean updateBillingRuleConfig(BillingRuleConfigRequest request);

    /**
     * 获取提醒配置
     *
     * @return 提醒配置JSON
     */
    String getRemindConfig();

    /**
     * 更新提醒配置
     *
     * @param request 配置请求
     * @return 是否成功
     */
    Boolean updateRemindConfig(RemindConfigRequest request);

    /**
     * 获取桌台数量配置
     *
     * @return 桌台数量
     */
    Integer getTableCountConfig();

    /**
     * 更新桌台数量配置
     *
     * @param count 桌台数量
     * @return 是否成功
     */
    Boolean updateTableCountConfig(Integer count);

    /**
     * 获取系统参数配置
     *
     * @return 系统参数配置JSON
     */
    String getSystemConfig();

    /**
     * 更新系统参数配置
     *
     * @param request 配置请求
     * @return 是否成功
     */
    Boolean updateSystemConfig(SystemConfigRequest request);
}
