package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;
import com.pindou.timer.event.TableStatusChangeEvent;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.BillingService;
import com.pindou.timer.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private final BillingService billingService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    // 默认计费配置（从数据库读取，这里先用默认值）
    private static final int DEFAULT_HOURLY_RATE = 30; // 每小时30元
    private static final int MIN_TABLE_COUNT = 1;
    private static final int MAX_TABLE_COUNT = 50;

    public TableServiceImpl(TableMapper tableMapper, OrderMapper orderMapper, BillingService billingService) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.billingService = billingService;
    }

    @Override
    public List<TableInfoResponse> getTableList(String status, Long categoryId) {
        log.info("获取桌台列表: status={}, categoryId={}", status, categoryId);

        // 构建查询条件
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Table::getStatus, status);
        }
        // 分类筛选（categoryId=0表示全部，不过滤）
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Table::getCategoryId, categoryId);
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
    public void updateTable(UpdateTableRequest request) {
        log.info("更新桌台信息: id={}, name={}", request.getId(), request.getName());

        // 查询桌台
        Table table = tableMapper.selectById(request.getId());
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND, "桌台不存在");
        }

        // 只有空闲状态的桌台可以修改名称和分类
        if (!"idle".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM, "只有空闲状态的桌台可以修改");
        }

        // 更新桌台信息
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            table.setName(request.getName().trim());
        }
        if (request.getCategoryId() != null) {
            table.setCategoryId(request.getCategoryId());
        }
        table.setUpdatedAt(System.currentTimeMillis());

        tableMapper.updateById(table);
        log.info("更新桌台成功: id={}", request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configTableCount(TableConfigRequest request, String userId, String username) {
        Integer tableCount = request.getTableCount();
        Long categoryId = request.getCategoryId();
        log.info("配置桌台数量: tableCount={}, categoryId={}, userId={}", tableCount, categoryId, userId);

        // 验证桌台数量范围
        if (tableCount < MIN_TABLE_COUNT || tableCount > MAX_TABLE_COUNT) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_COUNT_INVALID);
        }

        // 查询当前分类下的桌台数量
        LambdaQueryWrapper<Table> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Table::getCategoryId, categoryId);
        Long currentCount = tableMapper.selectCount(countWrapper);

        log.info("当前分类桌台数量: categoryId={}, count={}", categoryId, currentCount);

        if (tableCount > currentCount) {
            // 增加桌台
            int addCount = tableCount - currentCount.intValue();

            // 获取当前分类下所有存在的桌台ID
            LambdaQueryWrapper<Table> existingWrapper = new LambdaQueryWrapper<>();
            existingWrapper.eq(Table::getCategoryId, categoryId).select(Table::getId);
            List<Table> existingTables = tableMapper.selectList(existingWrapper);
            java.util.Set<Integer> existingIds = existingTables.stream()
                    .map(Table::getId)
                    .collect(java.util.stream.Collectors.toSet());

            // 查询当前最大的桌台ID（全局）
            LambdaQueryWrapper<Table> maxIdWrapper = new LambdaQueryWrapper<>();
            maxIdWrapper.orderByDesc(Table::getId).last("LIMIT 1");
            Table maxIdTable = tableMapper.selectOne(maxIdWrapper);
            int maxId = (maxIdTable != null ? maxIdTable.getId() : 0);

            // 收集可用的ID：优先使用1-maxId范围内未被使用的ID
            java.util.List<Integer> availableIds = new java.util.ArrayList<>();
            for (int i = 1; i <= maxId && availableIds.size() < addCount; i++) {
                if (!existingIds.contains(i)) {
                    availableIds.add(i);
                }
            }

            // 如果可用的ID不够，从maxId+1开始补充
            while (availableIds.size() < addCount) {
                availableIds.add(++maxId);
            }

            log.info("准备增加桌台: categoryId={}, addCount={}, availableIds={}", categoryId, addCount, availableIds);

            for (int i = 0; i < addCount; i++) {
                int newId = availableIds.get(i);
                Table table = new Table();
                table.setId(newId);
                table.setName("桌台" + newId);
                table.setCategoryId(categoryId);
                table.setStatus("idle");
                table.setPauseAccumulated(0);
                table.setReminded(0);
                table.setRemindIgnored(0);
                table.setCreatedAt(System.currentTimeMillis());
                table.setUpdatedAt(System.currentTimeMillis());
                tableMapper.insert(table);
                log.info("创建桌台: id={}, name={}, categoryId={}", newId, table.getName(), categoryId);
            }
            log.info("增加桌台完成: categoryId={}, addCount={}", categoryId, addCount);
        } else if (tableCount < currentCount) {
            // 减少桌台（只能删除该分类下空闲的桌台）
            int removeCount = currentCount.intValue() - tableCount;
            LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Table::getCategoryId, categoryId)
                    .eq(Table::getStatus, "idle")
                    .orderByDesc(Table::getId)
                    .last("LIMIT " + removeCount);

            List<Table> idleTables = tableMapper.selectList(wrapper);
            if (idleTables.size() < removeCount) {
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_HAS_USING);
            }

            for (Table table : idleTables) {
                tableMapper.deleteById(table.getId());
                log.info("删除桌台: id={}, categoryId={}", table.getId(), categoryId);
            }
            log.info("删除桌台完成: categoryId={}, removeCount={}", categoryId, removeCount);
        }

        log.info("配置桌台数量成功: categoryId={}, tableCount={}", categoryId, tableCount);
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

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

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

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

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

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

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

        long now = System.currentTimeMillis();

        // 更新订单状态和计算费用
        if (table.getCurrentOrderId() != null) {
            Order order = orderMapper.selectById(table.getCurrentOrderId());
            if (order != null) {
                // 计算总时长
                long start = table.getStartTime();
                int totalDuration = (int) ((now - start) / 1000);

                // 计算暂停时长
                int pauseDuration = table.getPauseAccumulated();
                if ("paused".equals(table.getStatus()) && table.getLastPauseTime() != null) {
                    pauseDuration += (int) ((now - table.getLastPauseTime()) / 1000);
                }

                // 计算计费时长
                int actualDuration = totalDuration - pauseDuration;

                // 计算费用
                AmountDetail amountDetail = billingService.calculateAmount(actualDuration, table.getPresetDuration());

                // 构建金额明细JSON
                JSONObject amountJson = new JSONObject();
                amountJson.set("normalAmount", amountDetail.getNormalAmount());
                amountJson.set("overtimeAmount", amountDetail.getOvertimeAmount());
                amountJson.set("totalAmount", amountDetail.getTotalAmount());
                amountJson.set("actualDuration", actualDuration);
                amountJson.set("billingType", amountDetail.getBillingType());
                amountJson.set("unitPrice", amountDetail.getUnitPrice());
                amountJson.set("overtimeRate", amountDetail.getOvertimeRate());

                // 更新订单
                order.setEndTime(now);
                order.setDuration(totalDuration);
                order.setPauseDuration(pauseDuration);
                order.setAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
                order.setAmountDetail(amountJson.toString());
                order.setStatus("completed");
                order.setPaidAt(now);
                order.setUpdatedAt(now);
                orderMapper.updateById(order);

                log.info("订单已完成: orderId={}, amount={}", order.getId(), amountDetail.getTotalAmount());
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
        table.setUpdatedAt(now);
        tableMapper.updateById(table);

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("结束计时成功: tableId={}", tableId);
    }

    @Override
    public BillResponse getBill(Integer tableId) {
        log.info("获取桌台账单: tableId={}", tableId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"using".equals(table.getStatus()) && !"paused".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 查询订单
        Order order = null;
        if (table.getCurrentOrderId() != null) {
            order = orderMapper.selectById(table.getCurrentOrderId());
        }
        if (order == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.ORDER_NOT_FOUND);
        }

        // 构建账单响应
        BillResponse response = new BillResponse();
        response.setOrderId(order.getId());
        response.setTableId(tableId);
        response.setTableName(table.getName());
        response.setStartTime(table.getStartTime());
        response.setEndTime(null);
        response.setOperatorName(order.getOperatorName());
        response.setStatus(order.getStatus());
        response.setPresetDuration(table.getPresetDuration());

        // 计算时长
        long now = System.currentTimeMillis();
        long start = table.getStartTime();
        int totalDuration = (int) ((now - start) / 1000);

        int pauseDuration = table.getPauseAccumulated();
        if ("paused".equals(table.getStatus()) && table.getLastPauseTime() != null) {
            pauseDuration += (int) ((now - table.getLastPauseTime()) / 1000);
        }

        int actualDuration = totalDuration - pauseDuration;

        response.setDuration(totalDuration);
        response.setPauseDuration(pauseDuration);
        response.setActualDuration(actualDuration);

        // 计算费用
        AmountDetail amountDetail = billingService.calculateAmount(actualDuration, table.getPresetDuration());
        response.setAmountDetail(amountDetail);

        log.info("获取桌台账单成功: tableId={}, amount={}", tableId, amountDetail.getTotalAmount());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTable(Integer tableId) {
        log.info("删除桌台: tableId={}", tableId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND, "桌台不存在");
        }

        // 只有空闲状态的桌台可以删除
        if (!"idle".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM, "只有空闲状态的桌台可以删除");
        }

        // 删除桌台
        tableMapper.deleteById(tableId);

        // 推送桌台状态变更事件
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("删除桌台成功: tableId={}", tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTables(List<Integer> tableIds) {
        log.info("批量删除桌台: tableIds={}", tableIds);

        if (tableIds == null || tableIds.isEmpty()) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM, "桌台ID列表不能为空");
        }

        // 查询所有要删除的桌台
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Table::getId, tableIds);
        List<Table> tables = tableMapper.selectList(wrapper);

        // 检查是否所有桌台都存在且都是空闲状态
        for (Integer tableId : tableIds) {
            Table table = tables.stream()
                    .filter(t -> t.getId().equals(tableId))
                    .findFirst()
                    .orElse(null);

            if (table == null) {
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND,
                        "桌台 " + tableId + " 不存在");
            }

            if (!"idle".equals(table.getStatus())) {
                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                        "桌台 " + table.getName() + " 不是空闲状态，无法删除");
            }
        }

        // 批量删除
        for (Integer tableId : tableIds) {
            tableMapper.deleteById(tableId);
            // 推送桌台状态变更事件
            eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));
        }

        log.info("批量删除桌台成功: count={}", tableIds.size());
    }

    /**
     * 转换为响应DTO
     */
    private TableInfoResponse convertToResponse(Table table) {
        TableInfoResponse response = new TableInfoResponse();
        response.setId(table.getId());
        response.setName(table.getName());
        response.setStatus(table.getStatus());
        response.setCategoryId(table.getCategoryId());
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
