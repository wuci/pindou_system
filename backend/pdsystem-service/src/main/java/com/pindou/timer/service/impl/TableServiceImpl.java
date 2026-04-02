package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Order;
import com.pindou.timer.entity.Table;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.event.TableStatusChangeEvent;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.service.BillingService;
import com.pindou.timer.service.ConfigService;
import com.pindou.timer.service.MemberService;
import com.pindou.timer.service.MemberLevelService;
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
    private final MemberMapper memberMapper;
    private final BillingService billingService;
    private final ConfigService configService;
    private final MemberService memberService;
    private final MemberLevelService memberLevelService;
    private final ApplicationEventPublisher eventPublisher;

    // 默认计费配置（从数据库读取，这里先用默认值）
    private static final int DEFAULT_HOURLY_RATE = 30; // 每小时30元
    private static final int MIN_TABLE_COUNT = 1;
    private static final int MAX_TABLE_COUNT = 50;

    public TableServiceImpl(TableMapper tableMapper, OrderMapper orderMapper, MemberMapper memberMapper, BillingService billingService, ConfigService configService, MemberService memberService, MemberLevelService memberLevelService, ApplicationEventPublisher eventPublisher) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.memberMapper = memberMapper;
        this.billingService = billingService;
        this.configService = configService;
        this.memberService = memberService;
        this.memberLevelService = memberLevelService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<TableInfoResponse> getTableList(String status, Long categoryId, String name) {
        log.info("获取桌台列表: status={}, categoryId={}, name={}", status, categoryId, name);

        // 构建查询条件
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Table::getStatus, status);
        }
        // 分类筛选（categoryId=0表示全部，不过滤）
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Table::getCategoryId, categoryId);
        }
        // 名称模糊搜索
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Table::getName, name.trim());
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

            // 获取所有已存在的桌台ID（全局，因为ID是唯一主键）
            LambdaQueryWrapper<Table> existingWrapper = new LambdaQueryWrapper<>();
            existingWrapper.select(Table::getId);
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
        log.info("开始计时: tableId={}, presetDuration={}, memberId={}, userId={}", tableId, request.getPresetDuration(), request.getMemberId(), userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态
        if (!"idle".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 计算初始费用（根据预设时长）
        Integer presetDuration = request.getPresetDuration() == 0 ? null : request.getPresetDuration();
        double originalAmount = 0.0;
        double finalAmount = 0.0;

        if (presetDuration != null) {
            // 使用预设时长和选择的渠道计算费用
            String channel = request.getChannel() != null ? request.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(channel, presetDuration, presetDuration);
            originalAmount = amountDetail.getTotalAmount();
            finalAmount = originalAmount;

            log.info("使用渠道 {} 计算初始费用: presetDuration={}秒, originalAmount={}", channel, presetDuration, originalAmount);

            // 如果选择了会员，应用折扣
            if (request.getMemberId() != null) {
                try {
                    CalculateDiscountRequest discountRequest = new CalculateDiscountRequest();
                    discountRequest.setOriginalAmount(java.math.BigDecimal.valueOf(originalAmount));
                    CalculateDiscountResponse discountResponse = memberService.calculateDiscount(request.getMemberId(), discountRequest);
                    if (discountResponse != null) {
                        finalAmount = discountResponse.getFinalAmount().doubleValue();
                        log.info("会员折扣: originalAmount={}, discountAmount={}, finalAmount={}",
                                originalAmount, discountResponse.getDiscountAmount(), finalAmount);
                    }
                } catch (Exception e) {
                    log.warn("计算会员折扣失败，使用原价: {}", e.getMessage());
                    finalAmount = originalAmount;
                }
            }
        }

        // 创建订单
        Order order = new Order();
        order.setId(IdUtil.simpleUUID());
        order.setTableId(tableId);
        order.setTableName(table.getName());
        order.setStartTime(System.currentTimeMillis());
        order.setDuration(0);
        order.setPauseDuration(0);
        order.setPresetDuration(presetDuration);
        order.setChannel(request.getChannel() != null ? request.getChannel() : "store"); // 默认店内
        order.setMemberId(request.getMemberId());
        order.setOriginalAmount(java.math.BigDecimal.valueOf(originalAmount));
        order.setStatus("active");
        order.setAmount(java.math.BigDecimal.valueOf(finalAmount));
        order.setAmountDetail("{\"normalFee\":" + originalAmount + ",\"overtimeFee\":0,\"totalFee\":" + finalAmount + "}");
        order.setOperatorId(userId);
        order.setOperatorName(username);
        // 设置支付方式
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "offline");
        order.setBalanceAmount(java.math.BigDecimal.ZERO);
        order.setOtherPaymentAmount(java.math.BigDecimal.ZERO);
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        orderMapper.insert(order);

        // 更新桌台状态
        table.setStatus("using");
        table.setCurrentOrderId(order.getId());
        table.setStartTime(System.currentTimeMillis());
        table.setPresetDuration(presetDuration);
        table.setPauseAccumulated(0);
        table.setLastPauseTime(null);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("开始计时成功: tableId={}, orderId={}, memberId={}, paymentMethod={}, originalAmount={}, finalAmount={}",
                tableId, order.getId(), request.getMemberId(), request.getPaymentMethod(), originalAmount, finalAmount);

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
    public TableInfoResponse extendTimer(Integer tableId, com.pindou.timer.dto.ExtendTableRequest request, String userId, String username) {
        log.info("续费时长: tableId={}, additionalDuration={}, userId={}", tableId, request.getAdditionalDuration(), userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_NOT_FOUND);
        }

        // 验证桌台状态 - 只有使用中或暂停状态可以续费
        if (!"using".equals(table.getStatus()) && !"paused".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.TABLE_STATUS_ERROR);
        }

        // 查询当前订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getId, table.getCurrentOrderId());
        Order order = orderMapper.selectOne(orderWrapper);

        if (order == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.ORDER_NOT_FOUND);
        }

        // 计算续费费用（根据续费时长和渠道）
        // 优先使用请求中的渠道，如果没有则使用订单中的渠道
        String extendChannel = request.getChannel() != null ? request.getChannel() : order.getChannel();
        if (extendChannel == null) {
            extendChannel = "store";  // 最终兜底使用店内渠道
        }

        AmountDetail extendAmountDetail = billingService.calculateAmount(
                extendChannel,
                request.getAdditionalDuration(),
                request.getAdditionalDuration()  // 续费时长本身作为预设时长来计算费用
        );
        double extendFee = extendAmountDetail.getTotalAmount();

        log.info("使用渠道 {} 计算续费费用: additionalDuration={}秒, extendFee={}", extendChannel, request.getAdditionalDuration(), extendFee);

        // 获取当前订单已存储的费用
        double currentAmount = order.getAmount() != null ? order.getAmount().doubleValue() : 0.0;
        double currentOriginalAmount = order.getOriginalAmount() != null ? order.getOriginalAmount().doubleValue() : 0.0;

        // 如果订单没有原价（老数据兼容），原价等于折后价
        if (currentOriginalAmount == 0) {
            currentOriginalAmount = currentAmount;
        }

        // 计算新的总原价（累加续费原价）
        double newOriginalAmount = currentOriginalAmount + extendFee;

        // 计算新的总折后价：先累加原价，然后对总原价计算折扣
        double newTotalAmount;
        if (request.getMemberId() != null) {
            try {
                // 对总原价计算会员折扣
                com.pindou.timer.dto.CalculateDiscountRequest discountRequest = new com.pindou.timer.dto.CalculateDiscountRequest();
                discountRequest.setOriginalAmount(java.math.BigDecimal.valueOf(newOriginalAmount));
                com.pindou.timer.dto.CalculateDiscountResponse discountResponse = memberService.calculateDiscount(request.getMemberId(), discountRequest);
                if (discountResponse != null && discountResponse.getFinalAmount() != null) {
                    newTotalAmount = discountResponse.getFinalAmount().doubleValue();
                    log.info("会员折扣（续费后总计）: memberId={}, totalOriginalAmount={}, discountAmount={}, finalAmount={}",
                        request.getMemberId(), newOriginalAmount, discountResponse.getDiscountAmount(), newTotalAmount);
                } else {
                    // 折扣计算失败，使用原价
                    newTotalAmount = newOriginalAmount;
                    log.warn("会员折扣计算失败，使用原价: memberId={}, totalOriginalAmount={}", request.getMemberId(), newOriginalAmount);
                }
            } catch (Exception e) {
                // 折扣计算异常，使用原价
                newTotalAmount = newOriginalAmount;
                log.warn("会员折扣计算异常，使用原价: memberId={}, error={}", request.getMemberId(), e.getMessage());
            }
        } else {
            // 没有会员，使用总原价
            newTotalAmount = newOriginalAmount;
        }

        log.info("续费前: originalAmount={}, amount={}", currentOriginalAmount, currentAmount);
        log.info("续费: extendFee={}", extendFee);
        log.info("续费后: originalAmount={}, amount={}", newOriginalAmount, newTotalAmount);

        // 更新订单的会员信息（如果有）
        if (request.getMemberId() != null) {
            order.setMemberId(request.getMemberId());
            log.info("更新订单会员信息: orderId={}, memberId={}", order.getId(), request.getMemberId());
        }

        // 更新订单的支付方式
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
            log.info("更新订单支付方式: orderId={}, paymentMethod={}", order.getId(), request.getPaymentMethod());
        }

        // 更新订单的预设时长和费用
        Integer currentPresetDuration = order.getPresetDuration();
        Integer newPresetDuration;

        if (currentPresetDuration == null) {
            // 如果当前是不限时，续费后变成具体时长
            newPresetDuration = request.getAdditionalDuration();
        } else {
            // 累加续费时长
            newPresetDuration = currentPresetDuration + request.getAdditionalDuration();
        }

        order.setPresetDuration(newPresetDuration);
        order.setOriginalAmount(java.math.BigDecimal.valueOf(newOriginalAmount));  // 更新原价
        order.setAmount(java.math.BigDecimal.valueOf(newTotalAmount));  // 更新折后价
        order.setUpdatedAt(System.currentTimeMillis());
        orderMapper.updateById(order);

        // 更新桌台的预设时长
        table.setPresetDuration(newPresetDuration);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("续费时长成功: tableId={}, orderId={}, oldPresetDuration={}, newPresetDuration={}, extendFee={}, newTotalAmount={}",
                tableId, order.getId(), currentPresetDuration, newPresetDuration, extendFee, newTotalAmount);

        return convertToResponse(table);
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
    public void endTimer(Integer tableId, EndTableRequest request, String userId, String username) {
        Long memberId = request != null ? request.getMemberId() : null;
        log.info("结束计时: tableId={}, userId={}, memberId={}", tableId, userId, memberId);

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
            // 重新查询订单以确保获取最新的累加金额
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

                // 判断订单状态：使用时长小于5分钟的订单标记为作废
                String orderStatus;
                if (actualDuration < 300) { // 5分钟 = 300秒
                    orderStatus = "cancelled";
                    log.info("订单使用时长不足5分钟，标记为作废: orderId={}, actualDuration={}", order.getId(), actualDuration);
                } else {
                    orderStatus = "completed";
                }

                // 优先使用订单中已有的累加费用，如果没有则重新计算
                double finalAmount;
                double originalAmount;
                AmountDetail amountDetail;

                // 打印日志用于调试
                log.info("结账时订单金额: orderId={}, amount={}, originalAmount={}", order.getId(), order.getAmount(), order.getOriginalAmount());

                if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
                    // 使用订单中累加的费用（初始费用 + 续费费用）
                    finalAmount = order.getAmount().doubleValue();

                    // 如果订单有原价，使用订单的原价；否则原价等于最终金额
                    if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                        originalAmount = order.getOriginalAmount().doubleValue();
                        log.info("使用订单存储的原价: orderId={}, originalAmount={}", order.getId(), originalAmount);
                    } else {
                        originalAmount = finalAmount;
                        log.info("订单无原价，使用最终金额作为原价: orderId={}, originalAmount={}", order.getId(), originalAmount);
                    }

                    // 仍然调用计费服务生成费用明细结构，但使用订单中的金额
                    amountDetail = new AmountDetail();
                    amountDetail.setTotalAmount(finalAmount);
                    amountDetail.setNormalAmount(finalAmount);
                    amountDetail.setOvertimeAmount(0.0);
                    amountDetail.setBillingType("preset");
                    amountDetail.setActualDuration(actualDuration);
                    amountDetail.setUnitPrice(finalAmount); // 使用总金额作为单价（套餐计费）
                    amountDetail.setOvertimeRate(1.0);

                    log.info("使用订单累加费用: orderId={}, originalAmount={}, finalAmount={}", order.getId(), originalAmount, finalAmount);
                } else {
                    // 订单中没有费用，重新计算，使用订单中存储的渠道
                    String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
                    amountDetail = billingService.calculateAmount(orderChannel, actualDuration, table.getPresetDuration());
                    originalAmount = amountDetail.getTotalAmount();
                    finalAmount = originalAmount;
                    log.info("使用渠道 {} 重新计算费用: orderId={}, originalAmount={}, finalAmount={}", orderChannel, order.getId(), originalAmount, finalAmount);
                }

                // 应用会员折扣（只有在订单没有应用过折扣的情况下才需要应用）
                // 判断条件：订单没有原价，或者原价等于最终价格（说明没有折扣）
                boolean needApplyDiscount = false;
                if (memberId != null) {
                    if (order.getOriginalAmount() == null || order.getOriginalAmount().doubleValue() == 0) {
                        // 订单没有原价，需要应用折扣
                        needApplyDiscount = true;
                        log.info("订单无原价，需要应用会员折扣: memberId={}, originalAmount={}", memberId, originalAmount);
                    } else if (Math.abs(order.getOriginalAmount().doubleValue() - order.getAmount().doubleValue()) < 0.01) {
                        // 原价等于最终价格，说明没有折扣，需要应用
                        needApplyDiscount = true;
                        log.info("订单未应用折扣，需要应用会员折扣: memberId={}, originalAmount={}", memberId, originalAmount);
                    } else {
                        log.info("订单已应用折扣，跳过: memberId={}, originalAmount={}, finalAmount={}", memberId, order.getOriginalAmount(), order.getAmount());
                    }
                }

                if (needApplyDiscount) {
                    log.info("应用会员折扣: memberId={}, originalAmount={}", memberId, originalAmount);
                    CalculateDiscountRequest discountRequest = new CalculateDiscountRequest();
                    discountRequest.setOriginalAmount(BigDecimal.valueOf(originalAmount));
                    CalculateDiscountResponse discountResponse = memberService.calculateDiscount(memberId, discountRequest);
                    finalAmount = discountResponse.getFinalAmount().doubleValue();
                    log.info("会员折扣已应用: originalAmount={}, discountRate={}, discountAmount={}, finalAmount={}",
                            originalAmount, discountResponse.getDiscountRate(),
                            discountResponse.getDiscountAmount(), finalAmount);
                }

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
                order.setOriginalAmount(BigDecimal.valueOf(originalAmount)); // 保存原价
                order.setAmount(BigDecimal.valueOf(finalAmount)); // 保存折后价格
                order.setMemberId(memberId); // 保存会员ID
                order.setAmountDetail(amountJson.toString());
                order.setStatus(orderStatus);
                order.setPaidAt("completed".equals(orderStatus) ? now : null); // 作废订单不记录支付时间
                order.setUpdatedAt(now);

                // 更新支付方式（从请求中获取，如果有的话）
                if (request != null && request.getPaymentMethod() != null) {
                    order.setPaymentMethod(request.getPaymentMethod());
                    log.info("更新订单支付方式: orderId={}, paymentMethod={}", order.getId(), request.getPaymentMethod());
                }

                // 处理余额扣除
                if (order.getMemberId() != null && order.getPaymentMethod() != null && "completed".equals(orderStatus)) {
                    String paymentMethod = order.getPaymentMethod();
                    java.math.BigDecimal finalAmountBigDecimal = java.math.BigDecimal.valueOf(finalAmount);

                    if ("balance".equals(paymentMethod) || "combined".equals(paymentMethod)) {
                        // 查询会员信息
                        Member member = memberMapper.selectById(order.getMemberId());
                        if (member == null) {
                            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND, "会员信息不存在");
                        }

                        java.math.BigDecimal balance = member.getBalance();
                        java.math.BigDecimal balanceAmount = java.math.BigDecimal.ZERO;
                        java.math.BigDecimal otherAmount = java.math.BigDecimal.ZERO;

                        if ("balance".equals(paymentMethod)) {
                            // 纯余额支付
                            if (balance.compareTo(finalAmountBigDecimal) < 0) {
                                throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                                    "余额不足，当前余额：" + balance + "元，需支付：" + finalAmountBigDecimal + "元");
                            }
                            balanceAmount = finalAmountBigDecimal;
                        } else {
                            // 组合支付：优先使用余额
                            if (balance.compareTo(finalAmountBigDecimal) >= 0) {
                                // 余额充足，全部用余额
                                balanceAmount = finalAmountBigDecimal;
                            } else {
                                // 余额不足，用完余额，剩余线下支付
                                balanceAmount = balance;
                                otherAmount = finalAmountBigDecimal.subtract(balance);
                            }
                        }

                        // 扣除余额
                        member.setBalance(member.getBalance().subtract(balanceAmount));
                        member.setTotalAmount(member.getTotalAmount().add(finalAmountBigDecimal));
                        memberMapper.updateById(member);

                        // 更新订单支付信息
                        order.setBalanceAmount(balanceAmount);
                        order.setOtherPaymentAmount(otherAmount);

                        log.info("余额扣除成功 - 会员ID:{}, 支付方式:{}, 扣除金额:{}, 余额剩余:{}",
                                member.getId(), paymentMethod, balanceAmount, member.getBalance());
                    }
                }

                orderMapper.updateById(order);

                // 更新会员累计消费（仅已完成的订单）
                if ("completed".equals(orderStatus) && memberId != null) {
                    memberService.updateTotalAmount(memberId, BigDecimal.valueOf(originalAmount));
                    log.info("会员累计消费已更新: memberId={}, amount={}", memberId, originalAmount);
                }

                log.info("订单已结束: orderId={}, status={}, originalAmount={}, finalAmount={}",
                        order.getId(), orderStatus, originalAmount, finalAmount);
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

        // 计算费用：优先使用订单中累加的费用
        AmountDetail amountDetail;

        // 打印日志用于调试
        log.info("获取账单时订单金额: orderId={}, amount={}", order.getId(), order.getAmount());

        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            // 使用订单中累加的费用
            double finalAmount = order.getAmount().doubleValue();

            // 直接创建费用明细，不调用billingService.calculateAmount（避免覆盖）
            amountDetail = new AmountDetail();
            amountDetail.setTotalAmount(finalAmount);
            amountDetail.setNormalAmount(finalAmount);
            amountDetail.setOvertimeAmount(0.0);
            amountDetail.setBillingType("preset");
            amountDetail.setActualDuration(actualDuration);
            amountDetail.setUnitPrice(finalAmount); // 使用总金额作为单价（套餐计费）
            amountDetail.setOvertimeRate(1.0);

            log.info("使用订单累加费用: tableId={}, finalAmount={}", tableId, finalAmount);
        } else {
            // 订单中没有费用，重新计算，使用订单中存储的渠道
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            amountDetail = billingService.calculateAmount(orderChannel, actualDuration, table.getPresetDuration());
            log.info("使用渠道 {} 重新计算费用: tableId={}, amount={}", orderChannel, tableId, amountDetail.getTotalAmount());
        }

        response.setAmountDetail(amountDetail);

        // 设置原价（如果有会员，originalAmount是折扣前的价格）
        if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
            response.setOriginalAmount(order.getOriginalAmount());
        } else {
            response.setOriginalAmount(java.math.BigDecimal.valueOf(amountDetail.getTotalAmount()));
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            try {
                com.pindou.timer.entity.Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    MemberInfo memberInfo = new MemberInfo();
                    memberInfo.setId(member.getId());
                    memberInfo.setName(member.getName());
                    memberInfo.setLevelName(getMemberLevelName(member.getLevelId()));
                    memberInfo.setDiscountRate(getMemberDiscountRate(member.getLevelId()));

                    // 计算折扣金额
                    double originalAmount = response.getOriginalAmount().doubleValue();
                    double finalAmount = amountDetail.getTotalAmount();
                    double discountAmount = originalAmount - finalAmount;

                    memberInfo.setDiscountAmount(java.math.BigDecimal.valueOf(discountAmount));
                    memberInfo.setFinalAmount(java.math.BigDecimal.valueOf(finalAmount));
                    memberInfo.setBalance(member.getBalance());

                    response.setMember(memberInfo);
                    log.info("会员信息: memberId={}, levelName={}, discountRate={}, originalAmount={}, discountAmount={}, finalAmount={}",
                            member.getId(), memberInfo.getLevelName(), memberInfo.getDiscountRate(),
                            originalAmount, discountAmount, finalAmount);
                }
            } catch (Exception e) {
                log.warn("获取会员信息失败: {}", e.getMessage());
            }
        }

        // 设置支付方式信息
        if (order.getPaymentMethod() != null) {
            response.setPaymentMethod(order.getPaymentMethod());
            response.setBalanceAmount(order.getBalanceAmount() != null ?
                order.getBalanceAmount().doubleValue() : null);
            response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null ?
                order.getOtherPaymentAmount().doubleValue() : null);
        }

        log.info("获取桌台账单成功: tableId={}, amount={}", tableId, amountDetail.getTotalAmount());
        return response;
    }

    /**
     * 获取会员等级名称
     */
    private String getMemberLevelName(Long levelId) {
        if (levelId == null) return "普通会员";
        try {
            com.pindou.timer.entity.MemberLevel level = memberLevelService.getById(levelId);
            return level != null ? level.getName() : "普通会员";
        } catch (Exception e) {
            return "普通会员";
        }
    }

    /**
     * 获取会员折扣率
     */
    private java.math.BigDecimal getMemberDiscountRate(Long levelId) {
        if (levelId == null) return java.math.BigDecimal.ONE;
        try {
            com.pindou.timer.entity.MemberLevel level = memberLevelService.getById(levelId);
            return level != null && level.getDiscountRate() != null
                    ? level.getDiscountRate()
                    : java.math.BigDecimal.ONE;
        } catch (Exception e) {
            return java.math.BigDecimal.ONE;
        }
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

        // 设置预定信息
        response.setReservationStatus(table.getReservationStatus());
        response.setReservationEndTime(table.getReservationEndTime());
        response.setReservationName(table.getReservationName());
        response.setReservationPhone(table.getReservationPhone());

        // 设置创建时间
        response.setCreatedAt(table.getCreatedAt());

        // 计算到点时间（开始时间 + 预设时长 + 延长时间）
        if (table.getStartTime() != null && table.getPresetDuration() != null) {
            // 获取延长时间配置（分钟），默认30分钟
            int extendTimeMinutes = getExtendTimeConfig();
            long baseEndTime = table.getStartTime() + table.getPresetDuration() * 1000L;
            long endTime = baseEndTime + extendTimeMinutes * 60 * 1000L;
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

            // 优先使用订单中存储的累加费用，如果没有则使用计费规则计算
            if (table.getCurrentOrderId() != null) {
                Order currentOrder = orderMapper.selectById(table.getCurrentOrderId());

                // 设置订单渠道
                if (currentOrder != null) {
                    response.setChannel(currentOrder.getChannel());
                }

                // 打印日志用于调试
                log.info("convertToResponse订单金额: orderId={}, amount={}, originalAmount={}, channel={}",
                    currentOrder.getId(), currentOrder.getAmount(), currentOrder.getOriginalAmount(), currentOrder.getChannel());

                if (currentOrder != null && currentOrder.getAmount() != null && currentOrder.getAmount().doubleValue() > 0) {
                    response.setAmount(currentOrder.getAmount().doubleValue());

                    // 设置原价
                    if (currentOrder.getOriginalAmount() != null && currentOrder.getOriginalAmount().doubleValue() > 0) {
                        response.setOriginalAmount(currentOrder.getOriginalAmount().doubleValue());
                    }

                    log.info("使用订单累加费用作为当前费用: tableId={}, amount={}, originalAmount={}",
                        table.getId(), currentOrder.getAmount().doubleValue(), response.getOriginalAmount());

                    // 填充会员信息
                    if (currentOrder.getMemberId() != null) {
                        response.setMemberId(currentOrder.getMemberId());
                        try {
                            Member member = memberService.getById(currentOrder.getMemberId());
                            if (member != null) {
                                response.setMemberName(member.getName());

                                // 获取会员等级信息
                                if (member.getLevelId() != null) {
                                    MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                                    if (memberLevel != null && memberLevel.getDiscountRate() != null) {
                                        response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                                    }
                                }

                                // 设置会员余额
                                if (member.getBalance() != null) {
                                    response.setMemberBalance(member.getBalance().doubleValue());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("获取会员信息失败: {}", e.getMessage());
                        }
                    }

                    // 设置支付方式信息
                    if (currentOrder.getPaymentMethod() != null) {
                        response.setPaymentMethod(currentOrder.getPaymentMethod());
                        response.setBalanceAmount(currentOrder.getBalanceAmount() != null ?
                            currentOrder.getBalanceAmount().doubleValue() : null);
                        response.setOtherPaymentAmount(currentOrder.getOtherPaymentAmount() != null ?
                            currentOrder.getOtherPaymentAmount().doubleValue() : null);
                    }
                } else {
                    // 如果订单中没有存储费用，则使用计费规则计算（初次计费）
                    // 使用订单中存储的渠道来计算费用
                    String orderChannel = currentOrder.getChannel() != null ? currentOrder.getChannel() : "store";
                    AmountDetail amountDetail = billingService.calculateAmount(orderChannel, (int) duration, table.getPresetDuration());
                    response.setAmount(amountDetail.getTotalAmount());
                    response.setOriginalAmount(amountDetail.getTotalAmount());

                    // 更新订单费用
                    currentOrder.setAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
                    currentOrder.setOriginalAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
                    currentOrder.setUpdatedAt(System.currentTimeMillis());
                    orderMapper.updateById(currentOrder);

                    log.info("初次计费并设置到订单: tableId={}, channel={}, amount={}", table.getId(), orderChannel, amountDetail.getTotalAmount());
                }
            } else {
                // 没有订单时，使用默认渠道计费规则计算
                AmountDetail amountDetail = billingService.calculateAmount("store", (int) duration, table.getPresetDuration());
                response.setAmount(amountDetail.getTotalAmount());
                response.setOriginalAmount(amountDetail.getTotalAmount());
                log.info("无订单，重新计算费用: tableId={}, amount={}", table.getId(), amountDetail.getTotalAmount());
            }
        } else {
            response.setDuration(0L);
            response.setPauseDuration(0L);
            response.setAmount(0.0);
        }

        return response;
    }

    /**
     * 获取延长时间配置（分钟）
     */
    private int getExtendTimeConfig() {
        try {
            String configStr = configService.getSystemConfig();
            if (configStr != null && !configStr.isEmpty()) {
                JSONObject config = JSONUtil.parseObj(configStr);
                Integer extendTime = config.getInt("extendTime");
                if (extendTime != null && extendTime > 0) {
                    return extendTime;
                }
            }
        } catch (Exception e) {
            log.warn("获取延长时间配置失败，使用默认值: {}", e.getMessage());
        }
        return 30; // 默认30分钟
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReservation(Integer tableId, ReservationRequest request, String userId, String username) {
        log.info("创建桌台预定: tableId={}, request={}, userId={}", tableId, request, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND, "桌台不存在");
        }

        // 检查桌台状态
        if (!"idle".equals(table.getStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                "桌台不是空闲状态，无法预定");
        }

        // 检查是否已有预定
        if ("reserved".equals(table.getReservationStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                "桌台已有预定，请先取消原预定");
        }

        // 检查预定时间
        long now = System.currentTimeMillis();
        if (request.getReservationEndTime() <= now) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                "预定截止时间必须晚于当前时间");
        }

        // 更新桌台预定信息
        table.setReservationStatus("reserved");
        table.setReservationEndTime(request.getReservationEndTime());
        table.setReservationName(request.getReservationName());
        table.setReservationPhone(request.getReservationPhone());
        table.setUpdatedAt(now);
        tableMapper.updateById(table);

        // 推送桌台状态变更事件
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("创建桌台预定成功: tableId={}, reservationName={}", tableId, request.getReservationName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Integer tableId, String userId, String username) {
        log.info("取消桌台预定: tableId={}, userId={}", tableId, userId);

        // 查询桌台
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.NOT_FOUND, "桌台不存在");
        }

        // 检查是否已预定
        if (!"reserved".equals(table.getReservationStatus())) {
            throw new BusinessException(com.pindou.timer.common.result.ErrorCode.INVALID_PARAM,
                "桌台没有被预定");
        }

        // 清除预定信息
        table.setReservationStatus("none");
        table.setReservationEndTime(null);
        table.setReservationName(null);
        table.setReservationPhone(null);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        // 推送桌台状态变更事件
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("取消桌台预定成功: tableId={}", tableId);
    }
}
