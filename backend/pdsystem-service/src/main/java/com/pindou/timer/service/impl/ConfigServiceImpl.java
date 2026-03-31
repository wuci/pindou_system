package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.BillingRuleConfigRequest;
import com.pindou.timer.dto.RemindConfigRequest;
import com.pindou.timer.entity.Config;
import com.pindou.timer.mapper.ConfigMapper;
import com.pindou.timer.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    /**
     * 计费规则配置键
     */
    private static final String BILLING_RULE_KEY = "billing_rule";

    /**
     * 提醒配置键
     */
    private static final String REMIND_CONFIG_KEY = "remind_config";

    /**
     * 桌台数量配置键
     */
    private static final String TABLE_COUNT_KEY = "table_count";

    @Resource
    private ConfigMapper configMapper;

    @Override
    public Map<String, String> getAllConfigs() {
        log.info("获取所有配置");

        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        List<Config> configs = configMapper.selectList(wrapper);

        Map<String, String> result = new HashMap<>();
        for (Config config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }

        return result;
    }

    @Override
    public String getBillingRuleConfig() {
        Config config = getConfigByKey(BILLING_RULE_KEY);
        if (config == null) {
            return getDefaultBillingRule();
        }
        return config.getConfigValue();
    }

    @Override
    public Boolean updateBillingRuleConfig(BillingRuleConfigRequest request) {
        log.info("更新计费规则配置: request={}", request);

        // 构建新的配置结构
        JSONObject configObject = new JSONObject();
        configObject.set("channels", request.getChannels());

        return updateConfig(BILLING_RULE_KEY, configObject.toString());
    }

    @Override
    public String getRemindConfig() {
        Config config = getConfigByKey(REMIND_CONFIG_KEY);
        if (config == null) {
            return getDefaultRemindConfig();
        }
        return config.getConfigValue();
    }

    @Override
    public Boolean updateRemindConfig(RemindConfigRequest request) {
        log.info("更新提醒配置: request={}", request);

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("threshold", request.getThreshold());
        jsonObject.set("soundEnabled", request.getSoundEnabled());
        jsonObject.set("repeatInterval", request.getRepeatInterval());

        return updateConfig(REMIND_CONFIG_KEY, jsonObject.toString());
    }

    @Override
    public Integer getTableCountConfig() {
        Config config = getConfigByKey(TABLE_COUNT_KEY);
        if (config == null) {
            return 20; // 默认20个桌台
        }

        try {
            JSONObject jsonObject = JSONUtil.parseObj(config.getConfigValue());
            return jsonObject.getInt("count");
        } catch (Exception e) {
            log.warn("解析桌台数量配置失败: {}", e.getMessage());
            return 20;
        }
    }

    @Override
    public Boolean updateTableCountConfig(Integer count) {
        log.info("更新桌台数量配置: count={}", count);

        // 验证数量范围
        if (count == null || count < 1 || count > 50) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "桌台数量必须在1-50之间");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("count", count);

        return updateConfig(TABLE_COUNT_KEY, jsonObject.toString());
    }

    /**
     * 根据键获取配置
     */
    private Config getConfigByKey(String key) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, key);
        return configMapper.selectOne(wrapper);
    }

    /**
     * 更新配置
     */
    private Boolean updateConfig(String key, String value) {
        Config existing = getConfigByKey(key);
        if (existing != null) {
            existing.setConfigValue(value);
            return configMapper.updateById(existing) > 0;
        } else {
            Config config = new Config();
            config.setConfigKey(key);
            config.setConfigValue(value);
            return configMapper.insert(config) > 0;
        }
    }

    /**
     * 获取默认计费规则
     */
    private String getDefaultBillingRule() {
        // 构建默认渠道计费规则
        JSONObject configObject = new JSONObject();

        // 店内渠道默认规则
        cn.hutool.json.JSONArray storeRules = new cn.hutool.json.JSONArray();
        storeRules.add(createRuleItem(1, 19.0, false));
        storeRules.add(createRuleItem(2, 35.0, false));
        storeRules.add(createRuleItem(4, 54.0, false));
        storeRules.add(createRuleItem(null, 68.0, true));

        JSONObject storeChannel = new JSONObject();
        storeChannel.set("channel", "store");
        storeChannel.set("channelName", "店内");
        storeChannel.set("rules", storeRules);

        // 美团渠道默认规则
        cn.hutool.json.JSONArray meituanRules = new cn.hutool.json.JSONArray();
        meituanRules.add(createRuleItem(1, 19.0, false));
        meituanRules.add(createRuleItem(2, 35.0, false));
        meituanRules.add(createRuleItem(4, 54.0, false));
        meituanRules.add(createRuleItem(null, 68.0, true));

        JSONObject meituanChannel = new JSONObject();
        meituanChannel.set("channel", "meituan");
        meituanChannel.set("channelName", "美团");
        meituanChannel.set("rules", meituanRules);

        // 大众点评渠道默认规则
        cn.hutool.json.JSONArray dianpingRules = new cn.hutool.json.JSONArray();
        dianpingRules.add(createRuleItem(1, 19.0, false));
        dianpingRules.add(createRuleItem(2, 35.0, false));
        dianpingRules.add(createRuleItem(4, 54.0, false));
        dianpingRules.add(createRuleItem(null, 68.0, true));

        JSONObject dianpingChannel = new JSONObject();
        dianpingChannel.set("channel", "dianping");
        dianpingChannel.set("channelName", "大众点评");
        dianpingChannel.set("rules", dianpingRules);

        // 组装所有渠道
        cn.hutool.json.JSONArray channelsArray = new cn.hutool.json.JSONArray();
        channelsArray.add(storeChannel);
        channelsArray.add(meituanChannel);
        channelsArray.add(dianpingChannel);

        configObject.set("channels", channelsArray);

        return configObject.toString();
    }

    /**
     * 创建规则项
     */
    private JSONObject createRuleItem(Integer hours, Double price, boolean unlimited) {
        JSONObject rule = new JSONObject();
        rule.set("hours", hours);
        rule.set("price", price);
        rule.set("unlimited", unlimited);
        return rule;
    }

    /**
     * 获取默认提醒配置
     */
    private String getDefaultRemindConfig() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("threshold", 300); // 5分钟
        jsonObject.set("soundEnabled", 1);
        jsonObject.set("repeatInterval", 60); // 1分钟
        return jsonObject.toString();
    }
}
