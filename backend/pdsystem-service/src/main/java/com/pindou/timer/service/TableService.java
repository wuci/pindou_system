package com.pindou.timer.service;

import com.pindou.timer.dto.StartTimerRequest;
import com.pindou.timer.dto.TableConfigRequest;
import com.pindou.timer.dto.TableInfoResponse;

import java.util.List;

/**
 * 桌台Service接口
 *
 * @author pindou
 * @since 1.0.0
 */
public interface TableService {

    /**
     * 获取桌台列表
     *
     * @param status 状态筛选（可选）
     * @return 桌台信息列表
     */
    List<TableInfoResponse> getTableList(String status);

    /**
     * 配置桌台数量
     *
     * @param request 配置请求
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void configTableCount(TableConfigRequest request, String userId, String username);

    /**
     * 开始计时
     *
     * @param tableId 桌台编号
     * @param request 开始计时请求
     * @param userId 操作用户ID
     * @param username 操作用户名
     * @return 桌台信息
     */
    TableInfoResponse startTimer(Integer tableId, StartTimerRequest request, String userId, String username);

    /**
     * 暂停计时
     *
     * @param tableId 桌台编号
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void pauseTimer(Integer tableId, String userId, String username);

    /**
     * 恢复计时
     *
     * @param tableId 桌台编号
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void resumeTimer(Integer tableId, String userId, String username);

    /**
     * 忽略提醒
     *
     * @param tableId 桌台编号
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void ignoreRemind(Integer tableId, String userId, String username);

    /**
     * 结束计时并结账
     *
     * @param tableId 桌台编号
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void endTimer(Integer tableId, String userId, String username);
}
