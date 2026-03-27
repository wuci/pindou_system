package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.dto.StartTimerRequest;
import com.pindou.timer.dto.TableConfigRequest;
import com.pindou.timer.dto.TableInfoResponse;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 桌台Service实现类
 *
 * @author pindou
 * @since 1.0.0
 */
@Slf4j
@Service
public class TableServiceImpl implements TableService {

    private final TableMapper tableMapper;
    private final OrderMapper orderMapper;

    // 默认计费配置（从数据库读取，这里先用默认值）
    private static final int DEFAULT_HOURLY_RATE = 30; // 每小时30元
    private static final int MIN_TABLE_COUNT = 1;
    private static final int MAX_TABLE_COUNT = 50;

    public TableServiceImpl(TableMapper tableMapper, OrderMapper orderMapper) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<TableInfoResponse> getTableList(String status) {
        log.info("获取桌台列表: status={}", status);

        // 构建查询条件
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Table::getStatus, status);
        }
        wrapper.orderByAsc(Table::getId);

        // 查询桌台列表
        List<Table> tables = tableMapper.selectList(wrapper);

        // 转换为响应DTO
        List<TableInfoResponse> responses = tables.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        log.info("获取桌台列表成功: count={}", responses.size());
        return responses;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configTableCount(TableConfigRequest request, String userId, String username) {
        Integer tableCount = request.getTableCount();
        log.info("配置桌台数量: tableCount={}, userId={}", tableCount, userId);

        // 验证桌台数量范围
        if (tableCount < MIN_TABLE_COUNT || tableCount > MAX_TABLE_COUNT) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_COUNT_INVALID);
        }

        // 查询当前桌台数量
        Long currentCount = tableMapper.selectCount(null);

        if (tableCount > currentCount) {
            // 增加桌台
            int addCount = tableCount - currentCount.intValue();

            // 查询当前最大的桌台ID
            LambdaQueryWrapper<Table> maxIdWrapper = new LambdaQueryWrapper<>();
            maxIdWrapper.orderByDesc(Table::getId).last("LIMIT 1");
            Table maxIdTable = tableMapper.selectOne(maxIdWrapper);
            int startId = (maxIdTable != null ? maxIdTable.getId() : 0) + 1;

            log.info("准备增加桌台: addCount={}, startId={}", addCount, startId);

            for (int i = 0; i < addCount; i++) {
                int newId = startId + i;
                Table table = new Table();
                table.setId(newId);
                table.setName("桌台" + newId);
                table.setStatus("idle");
                table.setPauseAccumulated(0);
                table.setReminded(0);
                table.setRemindIgnored(0);
                table.setCreatedAt(System.currentTimeMillis());
                table.setUpdatedAt(System.currentTimeMillis());
                tableMapper.insert(table);
                log.info("创建桌台: id={}, name={}", newId, table.getName());
            }
            log.info("增加桌台完成: addCount={}", addCount);
        } else if (tableCount < currentCount) {
            // 减少桌台（只能删除空闲的桌台）
            int removeCount = currentCount.intValue() - tableCount;
            LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Table::getStatus, "idle")
                    .orderByDesc(Table::getId)
                    .last("LIMIT " + removeCount);

            List<Table> idleTables = tableMapper.selectList(wrapper);
            if (idleTables.size() < removeCount) {
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_HAS_USING);
            }

            for (Table table : idleTables) {
                tableMapper.deleteById(table.getId());
            }
            log.info("删除桌台: removeCount={}", removeCount);
        }

        log.info("配置桌台数量成功: tableCount={}", tableCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableInfoResponse startTimer(Integer tableId, StartTimerRequest request, String userId, String username) {
        log.info("开始计时: tableId={}, presetDuration={}, userId={}", tableId, request.getPresetDuration(), userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"idle".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 创建订单
        Order order = new Order();
        order.setId(IdUtil.simpleUUID());
        order.setTableId(tableId);
        order.setTableName(table.getName());
        order.setStartTime(System.currentTimeMillis());
        order.setDuration(0);
        order.setPauseDuration(0);
        order.setPresetDuration(request.getPresetDuration() == 0 ? null : request.getPresetDuration());
        order.setStatus("active");
        order.setAmount(BigDecimal.ZERO);
        order.setAmountDetail("{\"normalFee\":0,\"overtimeFee\":0,\"totalFee\":0}");
        order.setOperatorId(userId);
        order.setOperatorName(username);
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        orderMapper.insert(order);

        // 更新桌台状态
        table.setStatus("using");
        table.setCurrentOrderId(order.getId());
        table.setStartTime(System.currentTimeMillis());
        table.setPresetDuration(request.getPresetDuration() == 0 ? null : request.getPresetDuration());
        table.setPauseAccumulated(0);
        table.setLastPauseTime(null);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        log.info("开始计时成功: tableId={}, orderId={}", tableId, order.getId());

        return convertToResponse(table);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseTimer(Integer tableId, String userId, String username) {
        log.info("暂停计时: tableId={}, userId={}", tableId, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"using".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 更新桌台状态
        table.setStatus("paused");
        table.setLastPauseTime(System.currentTimeMillis());
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        log.info("暂停计时成功: tableId={}", tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeTimer(Integer tableId, String userId, String username) {
        log.info("恢复计时: tableId={}, userId={}", tableId, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"paused".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 计算暂停时长
        long now = System.currentTimeMillis();
        long pauseTime = table.getLastPauseTime();
        long pauseDuration = (now - pauseTime) / 1000; // 转换为秒

        // 累加暂停时长
        table.setPauseAccumulated(table.getPauseAccumulated() + (int) pauseDuration);
        table.setStatus("using");
        table.setLastPauseTime(null);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        log.info("恢复计时成功: tableId={}, pauseDuration={}", tableId, pauseDuration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreRemind(Integer tableId, String userId, String username) {
        log.info("忽略提醒: tableId={}, userId={}", tableId, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 更新提醒忽略状态
        table.setRemindIgnored(1);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        log.info("忽略提醒成功: tableId={}", tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTimer(Integer tableId, String userId, String username) {
        log.info("结束计时: tableId={}, userId={}", tableId, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"using".equals(table.getStatus()) && !"paused".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 更新订单状态
        if (table.getCurrentOrderId() != null) {
            Order order = orderMapper.selectById(table.getCurrentOrderId());
            if (order != null) {
                order.setStatus("completed");
                order.setUpdatedAt(System.currentTimeMillis());
                orderMapper.updateById(order);
                log.info("订单已完成: orderId={}", order.getId());
            }
        }

        // 重置桌台状态
        table.setStatus("idle");
        table.setCurrentOrderId(null);
        table.setStartTime(null);
        table.setPresetDuration(null);
        table.setPauseAccumulated(0);
        table.setLastPauseTime(null);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        log.info("结束计时成功: tableId={}", tableId);
    }

    /**
     * 转换为响应DTO
     */
    private TableInfoResponse convertToResponse(Table table) {
        TableInfoResponse response = new TableInfoResponse();
        response.setId(table.getId());
        response.setName(table.getName());
        response.setStatus(table.getStatus());
        response.setCurrentOrderId(table.getCurrentOrderId());
        response.setStartTime(table.getStartTime());
        response.setPresetDuration(table.getPresetDuration());
        response.setReminded(table.getReminded());
        response.setRemindIgnored(table.getRemindIgnored());

        // 设置创建时间
        response.setCreatedAt(table.getCreatedAt());

        // 计算到点时间（开始时间 + 预设时长）
        if (table.getStartTime() != null && table.getPresetDuration() != null) {
            long endTime = table.getStartTime() + table.getPresetDuration() * 1000L;
            response.setEndTime(endTime);
        }

        // 计算时长和费用
        if ("using".equals(table.getStatus()) || "paused".equals(table.getStatus())) {
            long now = System.currentTimeMillis();
            long start = table.getStartTime();
            long totalElapsed = (now - start) / 1000; // 总经过时间（秒）

            // 计算实际使用时长（总时长 - 暂停时长）
            long pauseDuration = table.getPauseAccumulated();
            if ("paused".equals(table.getStatus()) && table.getLastPauseTime() != null) {
                // 如果当前是暂停状态，加上本次暂停时长
                pauseDuration += (now - table.getLastPauseTime()) / 1000;
            }

            long duration = totalElapsed - pauseDuration;
            response.setDuration(duration);
            response.setPauseDuration(pauseDuration);

            // 计算费用（简单按分钟计费）
            double hours = duration / 3600.0;
            double amount = hours * DEFAULT_HOURLY_RATE;
            response.setAmount(Math.round(amount * 100.0) / 100.0); // 保留两位小数
        } else {
            response.setDuration(0L);
            response.setPauseDuration(0L);
            response.setAmount(0.0);
        }

        return response;
    }
}
