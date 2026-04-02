package com.pindou.timer.service;

import com.pindou.timer.dto.BillResponse;
import com.pindou.timer.dto.EndTableRequest;
import com.pindou.timer.dto.ExtendTableRequest;
import com.pindou.timer.dto.ReservationRequest;
import com.pindou.timer.dto.StartTimerRequest;
import com.pindou.timer.dto.TableConfigRequest;
import com.pindou.timer.dto.TableInfoResponse;
import com.pindou.timer.dto.UpdateTableRequest;

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
     * @param categoryId 分类ID筛选（可选）
     * @param name 桌台名称模糊搜索（可选）
     * @return 桌台信息列表
     */
    List<TableInfoResponse> getTableList(String status, Long categoryId, String name);

    /**
     * 更新桌台信息
     *
     * @param request 更新请求
     */
    void updateTable(UpdateTableRequest request);

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
     * 续费时长
     *
     * @param tableId 桌台编号
     * @param request 续费请求
     * @param userId 操作用户ID
     * @param username 操作用户名
     * @return 桌台信息
     */
    TableInfoResponse extendTimer(Integer tableId, ExtendTableRequest request, String userId, String username);

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
     * @param request 结账请求（包含会员ID）
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void endTimer(Integer tableId, EndTableRequest request, String userId, String username);

    /**
     * 获取桌台账单
     *
     * @param tableId 桌台编号
     * @return 账单信息
     */
    BillResponse getBill(Integer tableId);

    /**
     * 删除桌台
     *
     * @param tableId 桌台编号
     */
    void deleteTable(Integer tableId);

    /**
     * 批量删除桌台
     *
     * @param tableIds 桌台编号列表
     */
    void batchDeleteTables(List<Integer> tableIds);

    /**
     * 创建桌台预定
     *
     * @param tableId 桌台编号
     * @param request 预定请求
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void createReservation(Integer tableId, ReservationRequest request, String userId, String username);

    /**
     * 取消桌台预定
     *
     * @param tableId 桌台编号
     * @param userId 操作用户ID
     * @param username 操作用户名
     */
    void cancelReservation(Integer tableId, String userId, String username);
}
