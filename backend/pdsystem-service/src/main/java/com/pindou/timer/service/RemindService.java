package com.pindou.timer.service;

import com.pindou.timer.dto.RemindInfo;
import com.pindou.timer.entity.Table;

import java.util.List;

/**
 * 提醒Service接口
 *
 * @author wuci
 * @date 2026-03-28
 */
public interface RemindService {

    /**
     * 检查并返回需要提醒的桌台列表
     *
     * @return 提醒信息列表
     */
    List<RemindInfo> checkReminders();

    /**
     * 忽略桌台提醒
     *
     * @param tableId 桌台ID
     * @return 是否成功
     */
    Boolean ignoreRemind(Integer tableId);
}
