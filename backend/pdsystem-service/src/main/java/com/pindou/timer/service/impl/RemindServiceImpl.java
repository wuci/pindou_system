package com.pindou.timer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.dto.RemindInfo;
import com.pindou.timer.entity.Config;
import com.pindou.timer.entity.Table;
import com.pindou.timer.mapper.ConfigMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.RemindService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 提醒Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class RemindServiceImpl implements RemindService {

    private static final String REMIND_CONFIG_KEY = "remind_config";

    /**
     * 提醒类型：即将到期
     */
    private static final String REMIND_TYPE_EXPIRING = "expiring";

    /**
     * 提醒类型：已超时
     */
    private static final String REMIND_TYPE_TIMEOUT = "timeout";

    @Resource
    private TableMapper tableMapper;

    @Resource
    private ConfigMapper configMapper;

    @Override
    public List<RemindInfo> checkReminders() {
        log.debug("检查提醒状态");

        List<RemindInfo> reminders = new ArrayList<>();

        // 获取提醒配置
        RemindConfig remindConfig = getRemindConfig();
        if (remindConfig == null) {
            return reminders;
        }

        long currentTime = System.currentTimeMillis();

        // 查询所有使用中的桌台
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Table::getStatus, "using");
        List<Table> tables = tableMapper.selectList(wrapper);

        for (Table table : tables) {
            if (table.getStartTime() == null || table.getCurrentOrderId() == null) {
                continue;
            }

            // 计算已用时长
            long usedDuration = (currentTime - table.getStartTime()) / 1000;
            // 减去累计暂停时长
            long actualDuration = usedDuration - table.getPauseAccumulated();

            // 获取预设时长
            Integer presetDuration = table.getPresetDuration();
            if (presetDuration == null || presetDuration <= 0) {
                // 不设时长，只检查超时（超过24小时）
                presetDuration = 24 * 60 * 60; // 24小时
            }

            // 检查即将到期（剩余时间 <= 阈值）
            if (actualDuration < presetDuration) {
                int remainingDuration = presetDuration - (int) actualDuration;
                if (remainingDuration <= remindConfig.threshold
                        && (table.getReminded() == null || table.getReminded() == 0)) {
                    reminders.add(createRemindInfo(table, REMIND_TYPE_EXPIRING,
                            (int) actualDuration, remainingDuration, 0));
                }
            } else {
                // 已超时
                int overtimeDuration = (int) actualDuration - presetDuration;
                // 超时后重复提醒
                if (table.getRemindIgnored() == null || table.getRemindIgnored() == 0) {
                    // 计算距离上次提醒的时间间隔
                    if (table.getLastPauseTime() != null) {
                        long timeSinceLastRemind = (currentTime - table.getLastPauseTime()) / 1000;
                        if (timeSinceLastRemind >= remindConfig.repeatInterval) {
                            reminders.add(createRemindInfo(table, REMIND_TYPE_TIMEOUT,
                                    (int) actualDuration, 0, overtimeDuration));
                            // 更新最后提醒时间
                            table.setLastPauseTime(currentTime);
                            tableMapper.updateById(table);
                        }
                    } else {
                        // 第一次超时提醒
                        reminders.add(createRemindInfo(table, REMIND_TYPE_TIMEOUT,
                                (int) actualDuration, 0, overtimeDuration));
                        table.setLastPauseTime(currentTime);
                        tableMapper.updateById(table);
                    }
                }
            }
        }

        if (!reminders.isEmpty()) {
            log.info("检测到 {} 个桌台需要提醒", reminders.size());
        }

        return reminders;
    }

    @Override
    public Boolean ignoreRemind(Integer tableId) {
        log.info("忽略桌台提醒: tableId={}", tableId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND, "桌台不存在");
        }

        // 标记提醒已被忽略
        table.setRemindIgnored(1);
        table.setLastPauseTime(System.currentTimeMillis());

        int result = tableMapper.updateById(table);

        log.info("忽略桌台提醒成功: tableId={}", tableId);
        return result > 0;
    }

    /**
     * 创建提醒信息
     */
    private RemindInfo createRemindInfo(Table table, String remindType,
                                           int usedDuration, int remainingDuration, int overtimeDuration) {
        RemindInfo info = new RemindInfo();
        info.setTableId(table.getId());
        info.setTableName(table.getName());
        info.setRemindType(remindType);
        info.setRemindTypeDesc(REMIND_TYPE_EXPIRING.equals(remindType) ? "即将到期" : "已超时");
        info.setStartTime(table.getStartTime());
        info.setPresetDuration(table.getPresetDuration());
        info.setUsedDuration(usedDuration);
        info.setRemainingDuration(remainingDuration);
        info.setOvertimeDuration(overtimeDuration);
        return info;
    }

    /**
     * 提醒配置
     */
    private static class RemindConfig {
        int threshold = 300; // 5分钟
        int repeatInterval = 60; // 1分钟
    }

    /**
     * 获取提醒配置
     */
    private RemindConfig getRemindConfig() {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, REMIND_CONFIG_KEY);
        Config config = configMapper.selectOne(wrapper);

        if (config == null) {
            return new RemindConfig();
        }

        try {
            RemindConfig remindConfig = new RemindConfig();
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(config.getConfigValue());
            remindConfig.threshold = jsonObject.getInt("threshold", 300);
            remindConfig.repeatInterval = jsonObject.getInt("repeatInterval", 60);
            return remindConfig;
        } catch (Exception e) {
            log.warn("解析提醒配置失败: {}", e.getMessage());
            return new RemindConfig();
        }
    }
}
