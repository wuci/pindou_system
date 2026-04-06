package com.pindou.timer.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.constants.TableConstants;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.*;
import com.pindou.timer.event.TableStatusChangeEvent;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.mapper.TableMapper;
import com.pindou.timer.mapper.ConsumptionRecordMapper;
import com.pindou.timer.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 桌台Service实现类
 *
 * @author wuci
 * @date 2026-04-06
 */
@Slf4j
@Service
public class TableServiceImpl implements TableService {

    @Autowired
    private TableMapper tableMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Autowired
    private BillingService billingService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private MemberDiscountService memberDiscountService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private OrderSettlementService orderSettlementService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DiscountRecordService discountRecordService;

    @Override
    public List<TableInfoResponse> getTableList(String status, Long categoryId, String name) {
        log.info("获取桌台列表: status={}, categoryId={}, name={}", status, categoryId, name);

        LambdaQueryWrapper<Table> wrapper = buildTableQueryWrapper(status, categoryId, name);
        List<Table> tables = tableMapper.selectList(wrapper);

        List<TableInfoResponse> responses = tables.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        log.info("获取桌台列表成功: count={}", responses.size());
        return responses;
    }

    /**
     * 构建桌台查询条件
     */
    private LambdaQueryWrapper<Table> buildTableQueryWrapper(String status, Long categoryId, String name) {
        LambdaQueryWrapper<Table> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Table::getStatus, status);
        }
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Table::getCategoryId, categoryId);
        }
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Table::getName, name.trim());
        }
        wrapper.orderByAsc(Table::getId);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTable(UpdateTableRequest request) {
        log.info("更新桌台信息: id={}, name={}", request.getId(), request.getName());

        Table table = tableMapper.selectById(request.getId());
        if (table == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌台不存在");
        }

        if (!TableConstants.isIdle(table.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "只有空闲状态的桌台可以修改");
        }

        updateTableFields(table, request);
        tableMapper.updateById(table);

        log.info("更新桌台成功: id={}", request.getId());
    }

    /**
     * 更新桌台字段
     */
    private void updateTableFields(Table table, UpdateTableRequest request) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            table.setName(request.getName().trim());
        }
        if (request.getCategoryId() != null) {
            table.setCategoryId(request.getCategoryId());
        }
        table.setUpdatedAt(System.currentTimeMillis());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configTableCount(TableConfigRequest request, String userId, String username) {
        Integer tableCount = request.getTableCount();
        Long categoryId = request.getCategoryId();
        log.info("配置桌台数量: tableCount={}, categoryId={}, userId={}", tableCount, categoryId, userId);

        validateTableCount(tableCount);

        Long currentCount = tableMapper.selectCount(
                new LambdaQueryWrapper<Table>().eq(Table::getCategoryId, categoryId)
        );
        log.info("当前分类桌台数量: categoryId={}, count={}", categoryId, currentCount);

        if (tableCount > currentCount) {
            addTables(categoryId, tableCount.intValue() - currentCount.intValue());
        } else if (tableCount < currentCount) {
            removeTables(categoryId, currentCount.intValue() - tableCount.intValue());
        }

        log.info("配置桌台数量成功: categoryId={}, tableCount={}", categoryId, tableCount);
    }

    /**
     * 验证桌台数量范围
     */
    private void validateTableCount(Integer tableCount) {
        if (tableCount < TableConstants.Defaults.MIN_TABLE_COUNT ||
            tableCount > TableConstants.Defaults.MAX_TABLE_COUNT) {
            throw new BusinessException(ErrorCode.TABLE_COUNT_INVALID);
        }
    }

    /**
     * 增加桌台
     */
    private void addTables(Long categoryId, int addCount) {
        List<Integer> availableIds = findAvailableTableIds(addCount);
        log.info("准备增加桌台: categoryId={}, addCount={}, availableIds={}", categoryId, addCount, availableIds);

        for (int newId : availableIds) {
            Table table = createNewTable(newId, categoryId);
            tableMapper.insert(table);
            log.info("创建桌台: id={}, name={}, categoryId={}", newId, table.getName(), categoryId);
        }
        log.info("增加桌台完成: categoryId={}, addCount={}", categoryId, addCount);
    }

    /**
     * 查找可用的桌台ID
     */
    private List<Integer> findAvailableTableIds(int neededCount) {
        List<Table> existingTables = tableMapper.selectList(
                new LambdaQueryWrapper<Table>().select(Table::getId)
        );
        java.util.Set<Integer> existingIds = existingTables.stream()
                .map(Table::getId)
                .collect(java.util.stream.Collectors.toSet());

        Table maxIdTable = tableMapper.selectOne(
                new LambdaQueryWrapper<Table>()
                        .orderByDesc(Table::getId)
                        .last("LIMIT 1")
        );
        int maxId = (maxIdTable != null ? maxIdTable.getId() : 0);

        List<Integer> availableIds = new ArrayList<>();
        for (int i = 1; i <= maxId && availableIds.size() < neededCount; i++) {
            if (!existingIds.contains(i)) {
                availableIds.add(i);
            }
        }
        while (availableIds.size() < neededCount) {
            availableIds.add(++maxId);
        }
        return availableIds;
    }

    /**
     * 创建新桌台
     */
    private Table createNewTable(int id, Long categoryId) {
        long now = System.currentTimeMillis();
        Table table = new Table();
        table.setId(id);
        table.setName("桌台" + id);
        table.setCategoryId(categoryId);
        table.setStatus(TableConstants.Status.IDLE);
        table.setPauseAccumulated(0);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setCreatedAt(now);
        table.setUpdatedAt(now);
        return table;
    }

    /**
     * 减少桌台
     */
    private void removeTables(Long categoryId, int removeCount) {
        List<Table> idleTables = tableMapper.selectList(
                new LambdaQueryWrapper<Table>()
                        .eq(Table::getCategoryId, categoryId)
                        .eq(Table::getStatus, TableConstants.Status.IDLE)
                        .orderByDesc(Table::getId)
                        .last("LIMIT " + removeCount)
        );

        if (idleTables.size() < removeCount) {
            throw new BusinessException(ErrorCode.TABLE_HAS_USING);
        }

        for (Table table : idleTables) {
            tableMapper.deleteById(table.getId());
            log.info("删除桌台: id={}, categoryId={}", table.getId(), categoryId);
        }
        log.info("删除桌台完成: categoryId={}, removeCount={}", categoryId, removeCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableInfoResponse startTimer(Integer tableId, StartTimerRequest request, String userId, String username) {
        log.info("开始计时: tableId={}, presetDuration={}, memberId={}, userId={}",
                tableId, request.getPresetDuration(), request.getMemberId(), userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.isValidForStartTimer(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        // 计算初始费用
        AmountCalculationResult amountCalc = calculateInitialAmount(request);

        // 创建订单
        Order order = createOrder(tableId, table.getName(), request, amountCalc, userId, username);
        orderMapper.insert(order);

        // 记录折扣信息到Redis
        if (amountCalc.getDiscountId() != null && amountCalc.getDiscountRate() != null) {
            discountRecordService.addDiscountRecord(order.getId(), amountCalc.getDiscountId(), amountCalc.getDiscountRate());
        }

        // 更新桌台状态
        updateTableForStartTimer(table, order, request.getPresetDuration());
        tableMapper.updateById(table);

        // 推送桌台状态变更
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("开始计时成功: tableId={}, orderId={}, memberId={}, originalAmount={}, finalAmount={}",
                tableId, order.getId(), request.getMemberId(), amountCalc.getOriginalAmount(), amountCalc.getFinalAmount());

        return convertToResponse(table);
    }

    /**
     * 计算初始金额
     */
    private AmountCalculationResult calculateInitialAmount(StartTimerRequest request) {
        Integer presetDuration = request.getPresetDuration() == 0 ? null : request.getPresetDuration();
        double originalAmount = 0.0;
        double finalAmount = 0.0;
        String discountId = null;
        BigDecimal discountRate = null;

        if (presetDuration != null) {
            String channel = request.getChannel() != null ? request.getChannel() : TableConstants.Channel.STORE;
            AmountDetail amountDetail = billingService.calculateAmount(channel, presetDuration, presetDuration);
            originalAmount = amountDetail.getTotalAmount();
            finalAmount = originalAmount;

            log.info("使用渠道 {} 计算初始费用: presetDuration={}秒, originalAmount={}", channel, presetDuration, originalAmount);

            // 判断是否应用活动折扣
            if (request.getDiscountId() != null && !request.getDiscountId().isEmpty()) {
                discountId = request.getDiscountId();
                // 根据折扣ID计算折扣
                CalculateDiscountResponse discountResponse = discountService.calculateDiscountById(
                        request.getDiscountId(),
                        BigDecimal.valueOf(originalAmount),
                        request.getMemberId()
                );
                if (discountResponse != null && discountResponse.getFinalAmount() != null) {
                    discountRate = discountResponse.getDiscountRate();
                    // 使用折扣率重新计算：原价 × 折扣率
                    finalAmount = originalAmount * discountRate.doubleValue();
                    log.info("应用活动折扣: originalAmount={}, finalAmount={}, discountRate={}, appliedDiscount={}",
                            originalAmount, finalAmount, discountRate, discountResponse.getAppliedDiscountName());
                }
            } else if (request.getMemberId() != null) {
                // 仅应用会员折扣
                finalAmount = memberDiscountService.applyDiscount(request.getMemberId(), originalAmount);
                log.info("应用会员折扣: originalAmount={}, finalAmount={}", originalAmount, finalAmount);
            }
        }

        return new AmountCalculationResult(originalAmount, finalAmount, discountId, discountRate);
    }

    /**
     * 创建订单
     */
    private Order createOrder(Integer tableId, String tableName, StartTimerRequest request,
                              AmountCalculationResult amountCalc, String userId, String username) {
        long now = System.currentTimeMillis();
        Order order = new Order();
        order.setId(IdUtil.simpleUUID());
        order.setTableId(tableId);
        order.setTableName(tableName);
        order.setStartTime(now);
        order.setDuration(0);
        order.setPauseDuration(0);
        order.setPresetDuration(request.getPresetDuration() == 0 ? null : request.getPresetDuration());
        order.setChannel(request.getChannel() != null ? request.getChannel() : TableConstants.Channel.STORE);
        order.setMemberId(request.getMemberId());
        order.setOriginalAmount(BigDecimal.valueOf(amountCalc.getOriginalAmount()));
        order.setStatus(TableConstants.OrderStatus.ACTIVE);
        order.setAmount(BigDecimal.valueOf(amountCalc.getFinalAmount()));
        order.setAmountDetail(buildSimpleAmountDetailJson(amountCalc.getFinalAmount()));
        order.setOperatorId(userId);
        order.setOperatorName(username);
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : TableConstants.PaymentMethod.OFFLINE);
        order.setBalanceAmount(BigDecimal.ZERO);
        order.setOtherPaymentAmount(BigDecimal.ZERO);

        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    /**
     * 更新桌台状态（开始计时）
     */
    private void updateTableForStartTimer(Table table, Order order, Integer presetDuration) {
        long now = System.currentTimeMillis();
        table.setStatus(TableConstants.Status.USING);
        table.setCurrentOrderId(order.getId());
        table.setStartTime(now);
        table.setPresetDuration(presetDuration);
        table.setPauseAccumulated(0);
        table.setLastPauseTime(null);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setUpdatedAt(now);
    }

    /**
     * 构建简单金额明细JSON
     */
    private String buildSimpleAmountDetailJson(double finalAmount) {
        return "{\"normalFee\":" + finalAmount + ",\"overtimeFee\":0,\"totalFee\":" + finalAmount + "}";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseTimer(Integer tableId, String userId, String username) {
        log.info("暂停计时: tableId={}, userId={}", tableId, userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.Status.USING.equals(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        table.setStatus(TableConstants.Status.PAUSED);
        table.setLastPauseTime(System.currentTimeMillis());
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("暂停计时成功: tableId={}", tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeTimer(Integer tableId, String userId, String username) {
        log.info("恢复计时: tableId={}, userId={}", tableId, userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.Status.PAUSED.equals(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        // 计算并累加暂停时长
        long now = System.currentTimeMillis();
        long pauseDuration = (now - table.getLastPauseTime()) / 1000;
        table.setPauseAccumulated(table.getPauseAccumulated() + (int) pauseDuration);
        table.setStatus(TableConstants.Status.USING);
        table.setLastPauseTime(null);
        table.setUpdatedAt(now);
        tableMapper.updateById(table);

        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("恢复计时成功: tableId={}, pauseDuration={}", tableId, pauseDuration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableInfoResponse extendTimer(Integer tableId, ExtendTableRequest request, String userId, String username) {
        log.info("续费时长: tableId={}, additionalDuration={}, userId={}", tableId, request.getAdditionalDuration(), userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.canExtend(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getId, table.getCurrentOrderId())
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        ExtendCalculationResult extendCalc = calculateExtendAmount(order, request);

        updateOrderForExtend(order, request, extendCalc);
        orderMapper.updateById(order);

        // 记录折扣信息到Redis
        if (extendCalc.getDiscountId() != null && extendCalc.getDiscountRate() != null) {
            discountRecordService.addDiscountRecord(order.getId(), extendCalc.getDiscountId(), extendCalc.getDiscountRate());
        }

        updateTableForExtend(table, extendCalc.getNewPresetDuration());
        tableMapper.updateById(table);

        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("续费时长成功: tableId={}, newPresetDuration={}, newTotalAmount={}",
                tableId, extendCalc.getNewPresetDuration(), extendCalc.getNewTotalAmount());

        return convertToResponse(table);
    }

    /**
     * 计算续费金额
     */
    private ExtendCalculationResult calculateExtendAmount(Order order, ExtendTableRequest request) {
        String extendChannel = request.getChannel() != null ? request.getChannel() : order.getChannel();
        if (extendChannel == null) {
            extendChannel = TableConstants.Channel.STORE;
        }

        AmountDetail extendAmountDetail = billingService.calculateAmount(
                extendChannel,
                request.getAdditionalDuration(),
                request.getAdditionalDuration()
        );
        double extendFee = extendAmountDetail.getTotalAmount();

        log.info("使用渠道 {} 计算续费费用: additionalDuration={}秒, extendFee={}", extendChannel, request.getAdditionalDuration(), extendFee);

        double currentAmount = order.getAmount() != null ? order.getAmount().doubleValue() : 0.0;
        double currentOriginalAmount = order.getOriginalAmount() != null ? order.getOriginalAmount().doubleValue() : 0.0;

        if (currentOriginalAmount == 0) {
            currentOriginalAmount = currentAmount;
        }

        double newOriginalAmount = currentOriginalAmount + extendFee;

        // 计算续费折扣信息
        ExtendDiscountCalculation discountCalc = calculateExtendDiscount(request, order, newOriginalAmount);
        double newTotalAmount = discountCalc.getFinalAmount();

        Integer currentPresetDuration = order.getPresetDuration();
        Integer newPresetDuration = currentPresetDuration == null
                ? request.getAdditionalDuration()
                : currentPresetDuration + request.getAdditionalDuration();

        return new ExtendCalculationResult(newOriginalAmount, newTotalAmount, newPresetDuration,
                discountCalc.getDiscountId(), discountCalc.getDiscountRate());
    }

    /**
     * 续费折扣计算结果
     */
    private static class ExtendDiscountCalculation {
        private final double finalAmount;
        private final String discountId;
        private final BigDecimal discountRate;

        public ExtendDiscountCalculation(double finalAmount, String discountId, BigDecimal discountRate) {
            this.finalAmount = finalAmount;
            this.discountId = discountId;
            this.discountRate = discountRate;
        }

        public double getFinalAmount() {
            return finalAmount;
        }

        public String getDiscountId() {
            return discountId;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }
    }

    /**
     * 计算续费折扣
     */
    private ExtendDiscountCalculation calculateExtendDiscount(ExtendTableRequest request, Order order, double newOriginalAmount) {
        // 判断是否应用新的活动折扣
        if (request.getDiscountId() != null && !request.getDiscountId().isEmpty()) {
            // 获取Redis中已有的折扣率列表
            List<BigDecimal> existingRates = discountRecordService.getDiscountRates(order.getId());

            // 获取新折扣的折扣率
            CalculateDiscountResponse discountResponse = discountService.calculateDiscountById(
                    request.getDiscountId(),
                    BigDecimal.valueOf(newOriginalAmount),
                    request.getMemberId()
            );
            if (discountResponse != null && discountResponse.getFinalAmount() != null) {
                BigDecimal newDiscountRate = discountResponse.getDiscountRate();
                // 使用Redis服务计算平均折扣率
                BigDecimal averageRate = discountRecordService.calculateAverageRate(existingRates, newDiscountRate);
                // 用平均折扣率计算最终金额：原价 × 平均折扣率
                double finalAmount = newOriginalAmount * averageRate.doubleValue();

                log.info("续费应用活动折扣: originalAmount={}, finalAmount={}, newDiscountRate={}, averageRate={}, existingCount={}",
                        newOriginalAmount, finalAmount, newDiscountRate, averageRate, existingRates.size());

                return new ExtendDiscountCalculation(finalAmount, request.getDiscountId(), newDiscountRate);
            }
        }

        // 没有新折扣，使用会员折扣或原价
        double finalAmount = newOriginalAmount;
        if (request.getMemberId() != null) {
            finalAmount = memberDiscountService.applyDiscount(request.getMemberId(), newOriginalAmount);
        }

        return new ExtendDiscountCalculation(finalAmount, null, null);
    }

    /**
     * 更新订单（续费）
     */
    private void updateOrderForExtend(Order order, ExtendTableRequest request, ExtendCalculationResult extendCalc) {
        long now = System.currentTimeMillis();

        if (request.getMemberId() != null) {
            order.setMemberId(request.getMemberId());
            log.info("更新订单会员信息: orderId={}, memberId={}", order.getId(), request.getMemberId());
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
            log.info("更新订单支付方式: orderId={}, paymentMethod={}", order.getId(), request.getPaymentMethod());
        }

        order.setPresetDuration(extendCalc.getNewPresetDuration());
        order.setOriginalAmount(BigDecimal.valueOf(extendCalc.getNewOriginalAmount()));
        order.setAmount(BigDecimal.valueOf(extendCalc.getNewTotalAmount()));
        order.setUpdatedAt(now);
    }

    /**
     * 更新桌台（续费）
     */
    private void updateTableForExtend(Table table, Integer newPresetDuration) {
        table.setPresetDuration(newPresetDuration);
        table.setUpdatedAt(System.currentTimeMillis());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreRemind(Integer tableId, String userId, String username) {
        log.info("忽略提醒: tableId={}, userId={}", tableId, userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

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

        Table table = validateTableForEndTimer(tableId);
        long now = System.currentTimeMillis();

        if (table.getCurrentOrderId() != null) {
            Order order = orderMapper.selectById(table.getCurrentOrderId());
            if (order != null) {
                processOrderEndTimer(order, table, request, memberId, now);
                orderMapper.updateById(order);

                updateMemberTotalAmountIfNeeded(memberId, order.getOriginalAmount(), order.getStatus());

                // 清除Redis中的折扣记录
                discountRecordService.clearDiscountRecords(order.getId());
            }
        }

        resetTableAfterEndTimer(table, now);
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("结束计时成功: tableId={}", tableId);
    }

    /**
     * 验证桌台状态（结账）
     */
    private Table validateTableForEndTimer(Integer tableId) {
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.canEndTimer(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        return table;
    }

    /**
     * 处理订单结账
     */
    private void processOrderEndTimer(Order order, Table table, EndTableRequest request, Long memberId, long now) {
        // 计算时长
        DurationCalculation durationCalc = orderSettlementService.calculateDuration(table, now);

        // 判断订单状态
        int invalidOrderThresholdSeconds = getInvalidOrderThresholdInSeconds();
        String orderStatus = orderSettlementService.determineOrderStatus(durationCalc.getActualDuration(), invalidOrderThresholdSeconds);

        // 计算费用
        OrderAmountCalculation amountCalc = orderSettlementService.calculateOrderAmount(order, table, durationCalc, memberId);

        // 应用会员折扣（如果需要）
        applyMemberDiscountIfNeeded(order, memberId, amountCalc);

        // 构建金额明细JSON
        String amountDetailJson = orderSettlementService.buildAmountDetailJson(amountCalc.getAmountDetail(), durationCalc.getActualDuration());

        // 更新订单
        orderSettlementService.updateOrderForEndTimer(order, durationCalc, amountCalc, amountDetailJson, orderStatus, request, now, memberId);

        // 处理余额扣除
        orderSettlementService.processBalanceDeduction(order, amountCalc.getFinalAmount(), orderStatus);

        log.info("订单已结束: orderId={}, status={}, originalAmount={}, finalAmount={}",
                order.getId(), orderStatus, amountCalc.getOriginalAmount(), amountCalc.getFinalAmount());
    }

    /**
     * 应用会员折扣（如果需要）
     */
    private void applyMemberDiscountIfNeeded(Order order, Long memberId, OrderAmountCalculation amountCalc) {
        if (memberId == null) {
            return;
        }

        boolean needApplyDiscount = memberDiscountService.needApplyDiscount(
                order.getOriginalAmount() != null ? order.getOriginalAmount().doubleValue() : null,
                amountCalc.getFinalAmount()
        );

        if (needApplyDiscount) {
            double finalAmount = memberDiscountService.applyDiscount(memberId, amountCalc.getOriginalAmount());
            amountCalc.setFinalAmount(finalAmount);

            log.info("会员折扣已应用: originalAmount={}, finalAmount={}",
                    amountCalc.getOriginalAmount(), finalAmount);
        }
    }

    /**
     * 更新会员累计消费（如果需要）
     */
    private void updateMemberTotalAmountIfNeeded(Long memberId, BigDecimal originalAmount, String orderStatus) {
        if (TableConstants.isOrderCompleted(orderStatus) && memberId != null && originalAmount != null) {
            memberService.updateTotalAmount(memberId, originalAmount);
            log.info("会员累计消费已更新: memberId={}, amount={}", memberId, originalAmount);
        }
    }

    /**
     * 重置桌台状态（结账后）
     */
    private void resetTableAfterEndTimer(Table table, long now) {
        table.setStatus(TableConstants.Status.IDLE);
        table.setCurrentOrderId(null);
        table.setStartTime(null);
        table.setPresetDuration(null);
        table.setPauseAccumulated(0);
        table.setLastPauseTime(null);
        table.setReminded(0);
        table.setRemindIgnored(0);
        table.setUpdatedAt(now);
        tableMapper.updateById(table);
    }

    /**
     * 获取无效订单时间阈值（秒）
     */
    private int getInvalidOrderThresholdInSeconds() {
        try {
            String systemConfigJson = configService.getSystemConfig();
            if (systemConfigJson != null && !systemConfigJson.isEmpty()) {
                JSONObject jsonObject = JSONUtil.parseObj(systemConfigJson);
                Integer invalidOrderTimeMinutes = jsonObject.getInt("invalidOrderTime");
                if (invalidOrderTimeMinutes != null && invalidOrderTimeMinutes > 0) {
                    int thresholdSeconds = invalidOrderTimeMinutes * 60;
                    log.info("从系统配置获取无效订单时间阈值: {}分钟 = {}秒", invalidOrderTimeMinutes, thresholdSeconds);
                    return thresholdSeconds;
                }
            }
        } catch (Exception e) {
            log.warn("解析系统配置失败，使用默认值0（不限制）: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public BillResponse getBill(Integer tableId) {
        log.info("获取桌台账单: tableId={}", tableId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.isInUse(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        Order order = table.getCurrentOrderId() != null
                ? orderMapper.selectById(table.getCurrentOrderId())
                : null;

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        BillResponse response = buildBillResponse(table, order);

        log.info("获取桌台账单成功: tableId={}, amount={}", tableId, response.getAmountDetail().getTotalAmount());
        return response;
    }

    /**
     * 构建账单响应
     */
    private BillResponse buildBillResponse(Table table, Order order) {
        BillResponse response = new BillResponse();
        response.setOrderId(order.getId());
        response.setTableId(table.getId());
        response.setTableName(table.getName());
        response.setStartTime(table.getStartTime());
        response.setEndTime(null);
        response.setOperatorName(order.getOperatorName());
        response.setStatus(order.getStatus());
        response.setPresetDuration(table.getPresetDuration());

        // 计算时长
        DurationCalculation durationCalc = calculateCurrentDuration(table);
        response.setDuration(durationCalc.getTotalDuration());
        response.setPauseDuration(durationCalc.getPauseDuration());
        response.setActualDuration(durationCalc.getActualDuration());

        // 计算费用
        AmountDetail amountDetail = calculateBillAmount(order, table, durationCalc.getActualDuration());
        response.setAmountDetail(amountDetail);

        // 设置原价
        if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
            response.setOriginalAmount(order.getOriginalAmount());
        } else {
            response.setOriginalAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            fillMemberInfo(response, order, amountDetail);
        }

        // 设置支付方式信息
        fillPaymentInfo(response, order, amountDetail);

        return response;
    }

    /**
     * 计算当前时长
     */
    private DurationCalculation calculateCurrentDuration(Table table) {
        long now = System.currentTimeMillis();
        return orderSettlementService.calculateDuration(table, now);
    }

    /**
     * 计算账单费用
     */
    private AmountDetail calculateBillAmount(Order order, Table table, int actualDuration) {
        log.info("获取账单时订单金额: orderId={}, amount={}", order.getId(), order.getAmount());

        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            double finalAmount = order.getAmount().doubleValue();
            return AmountDetail.builder()
                    .totalAmount(finalAmount)
                    .normalAmount(finalAmount)
                    .overtimeAmount(0.0)
                    .billingType("preset")
                    .actualDuration(actualDuration)
                    .unitPrice(finalAmount)
                    .overtimeRate(1.0)
                    .build();
        }

        String orderChannel = order.getChannel() != null ? order.getChannel() : TableConstants.Channel.STORE;
        AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, table.getPresetDuration());
        log.info("使用渠道 {} 重新计算费用: tableId={}, amount={}", orderChannel, table.getId(), amountDetail.getTotalAmount());
        return amountDetail;
    }

    /**
     * 填充会员信息
     */
    private void fillMemberInfo(BillResponse response, Order order, AmountDetail amountDetail) {
        try {
            Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                MemberInfo memberInfo = new MemberInfo();
                memberInfo.setId(member.getId());
                memberInfo.setName(member.getName());
                memberInfo.setLevelName(getMemberLevelName(member.getLevelId()));
                memberInfo.setDiscountRate(getMemberDiscountRate(member.getLevelId()));

                double originalAmount = response.getOriginalAmount().doubleValue();
                double finalAmount = amountDetail.getTotalAmount();
                double discountAmount = originalAmount - finalAmount;

                memberInfo.setDiscountAmount(BigDecimal.valueOf(discountAmount));
                memberInfo.setFinalAmount(BigDecimal.valueOf(finalAmount));
                memberInfo.setBalance(member.getBalance());

                response.setMember(memberInfo);
            }
        } catch (Exception e) {
            log.warn("获取会员信息失败: {}", e.getMessage());
        }
    }

    /**
     * 填充支付方式信息
     */
    private void fillPaymentInfo(BillResponse response, Order order, AmountDetail amountDetail) {
        if (order.getPaymentMethod() == null) {
            return;
        }

        response.setPaymentMethod(order.getPaymentMethod());

        if (!TableConstants.isBalancePayment(order.getPaymentMethod())) {
            response.setBalanceAmount(order.getBalanceAmount() != null
                    ? order.getBalanceAmount().doubleValue() : null);
            response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null
                    ? order.getOtherPaymentAmount().doubleValue() : null);
            return;
        }

        // 动态计算余额支付金额
        if ((order.getBalanceAmount() == null || order.getBalanceAmount().doubleValue() == 0)
                && order.getMemberId() != null) {
            calculateBalancePayment(response, order, amountDetail);
        } else {
            response.setBalanceAmount(order.getBalanceAmount() != null
                    ? order.getBalanceAmount().doubleValue() : null);
            response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null
                    ? order.getOtherPaymentAmount().doubleValue() : null);
        }
    }

    /**
     * 计算余额支付金额
     */
    private void calculateBalancePayment(BillResponse response, Order order, AmountDetail amountDetail) {
        try {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null && member.getBalance() != null) {
                double totalAmount = amountDetail.getTotalAmount();
                double balance = member.getBalance().doubleValue();

                double balanceAmount;
                double otherAmount;

                if (TableConstants.PaymentMethod.BALANCE.equals(order.getPaymentMethod())) {
                    balanceAmount = totalAmount;
                    otherAmount = 0;
                } else {
                    if (balance >= totalAmount) {
                        balanceAmount = totalAmount;
                        otherAmount = 0;
                    } else {
                        balanceAmount = balance;
                        otherAmount = totalAmount - balance;
                    }
                }

                response.setBalanceAmount(balanceAmount);
                response.setOtherPaymentAmount(otherAmount);

                log.info("支付方式动态计算 - 支付方式:{}, 订单总额:{}, 会员余额:{}, 余额支付:{}, 线下支付:{}",
                        order.getPaymentMethod(), totalAmount, balance, balanceAmount, otherAmount);
            }
        } catch (Exception e) {
            log.warn("动态计算支付金额失败: {}", e.getMessage());
            response.setBalanceAmount(order.getBalanceAmount() != null
                    ? order.getBalanceAmount().doubleValue() : null);
            response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null
                    ? order.getOtherPaymentAmount().doubleValue() : null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTable(Integer tableId) {
        log.info("删除桌台: tableId={}", tableId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌台不存在");
        }

        if (!TableConstants.isIdle(table.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "只有空闲状态的桌台可以删除");
        }

        tableMapper.deleteById(tableId);
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("删除桌台成功: tableId={}", tableId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTables(List<Integer> tableIds) {
        log.info("批量删除桌台: tableIds={}", tableIds);

        if (tableIds == null || tableIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "桌台ID列表不能为空");
        }

        validateTablesForDeletion(tableIds);

        for (Integer tableId : tableIds) {
            tableMapper.deleteById(tableId);
            eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));
        }

        log.info("批量删除桌台成功: count={}", tableIds.size());
    }

    /**
     * 验证桌台是否可以删除
     */
    private void validateTablesForDeletion(List<Integer> tableIds) {
        List<Table> tables = tableMapper.selectList(
                new LambdaQueryWrapper<Table>().in(Table::getId, tableIds)
        );

        for (Integer tableId : tableIds) {
            Table table = tables.stream()
                    .filter(t -> t.getId().equals(tableId))
                    .findFirst()
                    .orElse(null);

            if (table == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "桌台 " + tableId + " 不存在");
            }

            if (!TableConstants.isIdle(table.getStatus())) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                        "桌台 " + table.getName() + " 不是空闲状态，无法删除");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReservation(Integer tableId, ReservationRequest request, String userId, String username) {
        log.info("创建桌台预定: tableId={}, request={}, userId={}", tableId, request, userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌台不存在");
        }

        if (!TableConstants.isIdle(table.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "桌台不是空闲状态，无法预定");
        }

        if (TableConstants.ReservationStatus.RESERVED.equals(table.getReservationStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "桌台已有预定，请先取消原预定");
        }

        long now = System.currentTimeMillis();
        if (request.getReservationEndTime() <= now) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "预定截止时间必须晚于当前时间");
        }

        table.setReservationStatus(TableConstants.ReservationStatus.RESERVED);
        table.setReservationEndTime(request.getReservationEndTime());
        table.setReservationName(request.getReservationName());
        table.setReservationPhone(request.getReservationPhone());
        table.setUpdatedAt(now);
        tableMapper.updateById(table);

        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("创建桌台预定成功: tableId={}, reservationName={}", tableId, request.getReservationName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Integer tableId, String userId, String username) {
        log.info("取消桌台预定: tableId={}, userId={}", tableId, userId);

        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "桌台不存在");
        }

        if (!TableConstants.ReservationStatus.RESERVED.equals(table.getReservationStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "桌台没有被预定");
        }

        table.setReservationStatus(TableConstants.ReservationStatus.NONE);
        table.setReservationEndTime(null);
        table.setReservationName(null);
        table.setReservationPhone(null);
        table.setUpdatedAt(System.currentTimeMillis());
        tableMapper.updateById(table);

        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("取消桌台预定成功: tableId={}", tableId);
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
        response.setReservationStatus(table.getReservationStatus());
        response.setReservationEndTime(table.getReservationEndTime());
        response.setReservationName(table.getReservationName());
        response.setReservationPhone(table.getReservationPhone());
        response.setCreatedAt(table.getCreatedAt());

        // 计算到点时间
        if (table.getStartTime() != null && table.getPresetDuration() != null) {
            int extendTimeMinutes = getExtendTimeConfig();
            long baseEndTime = table.getStartTime() + table.getPresetDuration() * 1000L;
            long endTime = baseEndTime + extendTimeMinutes * 60 * 1000L;
            response.setEndTime(endTime);
        }

        // 计算时长和费用
        if (TableConstants.isInUse(table.getStatus())) {
            fillDurationAndAmount(response, table);
        } else {
            response.setDuration(0L);
            response.setPauseDuration(0L);
            response.setAmount(0.0);
        }

        return response;
    }

    /**
     * 填充时长和费用信息
     */
    private void fillDurationAndAmount(TableInfoResponse response, Table table) {
        long now = System.currentTimeMillis();
        long start = table.getStartTime();
        long totalElapsed = (now - start) / 1000;

        long pauseDuration = table.getPauseAccumulated();
        if (TableConstants.Status.PAUSED.equals(table.getStatus()) && table.getLastPauseTime() != null) {
            pauseDuration += (now - table.getLastPauseTime()) / 1000;
        }

        long duration = totalElapsed - pauseDuration;
        response.setDuration(duration);
        response.setPauseDuration(pauseDuration);

        if (table.getCurrentOrderId() != null) {
            Order currentOrder = orderMapper.selectById(table.getCurrentOrderId());
            if (currentOrder != null) {
                response.setChannel(currentOrder.getChannel());

                if (currentOrder.getAmount() != null && currentOrder.getAmount().doubleValue() > 0) {
                    response.setAmount(currentOrder.getAmount().doubleValue());
                    if (currentOrder.getOriginalAmount() != null && currentOrder.getOriginalAmount().doubleValue() > 0) {
                        response.setOriginalAmount(currentOrder.getOriginalAmount().doubleValue());
                    }
                    fillOrderMemberInfo(response, currentOrder);
                    fillOrderPaymentInfo(response, currentOrder);
                } else {
                    calculateAndSetAmount(response, table, currentOrder, (int) duration);
                }
            }
        } else {
            AmountDetail amountDetail = billingService.calculateAmount(TableConstants.Channel.STORE, (int) duration, table.getPresetDuration());
            response.setAmount(amountDetail.getTotalAmount());
            response.setOriginalAmount(amountDetail.getTotalAmount());
        }
    }

    /**
     * 填充订单会员信息
     */
    private void fillOrderMemberInfo(TableInfoResponse response, Order order) {
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
            try {
                Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    response.setMemberName(member.getName());
                    if (member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                        if (memberLevel != null && memberLevel.getDiscountRate() != null) {
                            response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                        }
                    }
                    if (member.getBalance() != null) {
                        response.setMemberBalance(member.getBalance().doubleValue());
                    }
                }
            } catch (Exception e) {
                log.warn("获取会员信息失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 填充订单支付方式信息
     */
    private void fillOrderPaymentInfo(TableInfoResponse response, Order order) {
        if (order.getPaymentMethod() != null) {
            response.setPaymentMethod(order.getPaymentMethod());
            response.setBalanceAmount(order.getBalanceAmount() != null
                    ? order.getBalanceAmount().doubleValue() : null);
            response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null
                    ? order.getOtherPaymentAmount().doubleValue() : null);
        }
    }

    /**
     * 计算并设置金额
     */
    private void calculateAndSetAmount(TableInfoResponse response, Table table, Order currentOrder, int duration) {
        String orderChannel = currentOrder.getChannel() != null ? currentOrder.getChannel() : TableConstants.Channel.STORE;
        AmountDetail amountDetail = billingService.calculateAmount(orderChannel, duration, table.getPresetDuration());
        response.setAmount(amountDetail.getTotalAmount());
        response.setOriginalAmount(amountDetail.getTotalAmount());

        currentOrder.setAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
        currentOrder.setOriginalAmount(BigDecimal.valueOf(amountDetail.getTotalAmount()));
        currentOrder.setUpdatedAt(System.currentTimeMillis());
        orderMapper.updateById(currentOrder);

        log.info("初次计费并设置到订单: tableId={}, channel={}, amount={}", table.getId(), orderChannel, amountDetail.getTotalAmount());
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
        return TableConstants.Defaults.DEFAULT_EXTEND_TIME_MINUTES;
    }

    /**
     * 获取会员等级名称
     */
    private String getMemberLevelName(Long levelId) {
        if (levelId == null) return "普通会员";
        try {
            MemberLevel level = memberLevelService.getById(levelId);
            return level != null ? level.getName() : "普通会员";
        } catch (Exception e) {
            return "普通会员";
        }
    }

    /**
     * 获取会员折扣率
     */
    private BigDecimal getMemberDiscountRate(Long levelId) {
        if (levelId == null) return BigDecimal.ONE;
        try {
            MemberLevel level = memberLevelService.getById(levelId);
            return level != null && level.getDiscountRate() != null
                    ? level.getDiscountRate()
                    : BigDecimal.ONE;
        } catch (Exception e) {
            return BigDecimal.ONE;
        }
    }

    /**
     * 金额计算结果
     */
    private static class AmountCalculationResult {
        private final double originalAmount;
        private final double finalAmount;
        private final String discountId;
        private final BigDecimal discountRate;

        public AmountCalculationResult(double originalAmount, double finalAmount, String discountId, BigDecimal discountRate) {
            this.originalAmount = originalAmount;
            this.finalAmount = finalAmount;
            this.discountId = discountId;
            this.discountRate = discountRate;
        }

        public AmountCalculationResult(double originalAmount, double finalAmount) {
            this(originalAmount, finalAmount, null, null);
        }

        public double getOriginalAmount() {
            return originalAmount;
        }

        public double getFinalAmount() {
            return finalAmount;
        }

        public String getDiscountId() {
            return discountId;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }
    }

    /**
     * 续费计算结果
     */
    private static class ExtendCalculationResult {
        private final double newOriginalAmount;
        private final double newTotalAmount;
        private final Integer newPresetDuration;
        private final String discountId;
        private final BigDecimal discountRate;

        public ExtendCalculationResult(double newOriginalAmount, double newTotalAmount, Integer newPresetDuration,
                                       String discountId, BigDecimal discountRate) {
            this.newOriginalAmount = newOriginalAmount;
            this.newTotalAmount = newTotalAmount;
            this.newPresetDuration = newPresetDuration;
            this.discountId = discountId;
            this.discountRate = discountRate;
        }

        public ExtendCalculationResult(double newOriginalAmount, double newTotalAmount, Integer newPresetDuration) {
            this(newOriginalAmount, newTotalAmount, newPresetDuration, null, null);
        }

        public double getNewOriginalAmount() {
            return newOriginalAmount;
        }

        public double getNewTotalAmount() {
            return newTotalAmount;
        }

        public Integer getNewPresetDuration() {
            return newPresetDuration;
        }

        public String getDiscountId() {
            return discountId;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }
    }
}
