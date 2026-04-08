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
import com.pindou.timer.mapper.MemberLevelMapper;
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
    private MemberLevelMapper memberLevelMapper;

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
    public List<TableInfoResponse> getTableList(String status, Long categoryId, String name, Boolean isOvertime, Integer remainingMinutes) {
        long startTime = System.currentTimeMillis();
        log.info("获取桌台列表: status={}, categoryId={}, name={}, isOvertime={}, remainingMinutes={}",
                status, categoryId, name, isOvertime, remainingMinutes);

        long queryStart = System.currentTimeMillis();
        LambdaQueryWrapper<Table> wrapper = buildTableQueryWrapper(status, categoryId, name);
        List<Table> tables = tableMapper.selectList(wrapper);
        log.info("查询桌台数据耗时: {}ms, count={}", System.currentTimeMillis() - queryStart, tables.size());

        // 预加载配置（避免每个台台都查询一次）
        long preloadConfigStart = System.currentTimeMillis();
        int extendTimeMinutes = getExtendTimeConfig();
        log.info("预加载配置耗时: {}ms", System.currentTimeMillis() - preloadConfigStart);

        // 批量预加载数据，优化性能
        long preloadStart = System.currentTimeMillis();
        java.util.Map<String, Order> orderMap = preloadOrders(tables);
        java.util.Map<Long, Member> memberMap = preloadMembers(orderMap);
        java.util.Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);
        log.info("批量预加载耗时: {}ms (orders={}, members={}, levels={})",
                System.currentTimeMillis() - preloadStart, orderMap.size(), memberMap.size(), memberLevelMap.size());

        // 转换为响应对象
        long convertStart = System.currentTimeMillis();
        final int finalExtendTimeMinutes = extendTimeMinutes;
        List<TableInfoResponse> responses = tables.stream()
                .map(table -> convertToResponse(table, orderMap, memberMap, memberLevelMap, finalExtendTimeMinutes))
                .collect(Collectors.toList());
        log.info("转换响应对象耗时: {}ms", System.currentTimeMillis() - convertStart);

        // 根据超时和剩余时间条件过滤
        if (isOvertime != null && isOvertime) {
            responses = responses.stream()
                    .filter(this::isOvertime)
                    .collect(Collectors.toList());
        }

        if (remainingMinutes != null && remainingMinutes > 0) {
            final long currentTime = System.currentTimeMillis();
            final long remainingMillis = remainingMinutes * 60 * 1000L;
            responses = responses.stream()
                    .filter(r -> willExpireWithin(r, currentTime, remainingMillis))
                    .collect(Collectors.toList());
        }

        long endTime = System.currentTimeMillis();
        log.info("获取桌台列表成功: count={},耗时={}ms", responses.size(), endTime - startTime);
        return responses;
    }

    /**
     * 批量预加载订单数据
     *
     * @param tables 桌台列表
     * @return 订单ID -> 订单的映射
     */
    private java.util.Map<String, Order> preloadOrders(List<Table> tables) {
        // 收集所有订单ID
        List<String> orderIds = tables.stream()
                .map(Table::getCurrentOrderId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 批量查询订单
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        return orders.stream().collect(Collectors.toMap(Order::getId, o -> o, (a, b) -> a));
    }

    /**
     * 批量预加载会员数据
     *
     * @param orderMap 订单映射
     * @return 会员ID -> 会员的映射
     */
    private java.util.Map<Long, Member> preloadMembers(java.util.Map<String, Order> orderMap) {
        // 收集所有会员ID
        List<Long> memberIds = orderMap.values().stream()
                .map(Order::getMemberId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 批量查询会员
        List<Member> members = memberMapper.selectBatchIds(memberIds);
        return members.stream().collect(Collectors.toMap(Member::getId, m -> m, (a, b) -> a));
    }

    /**
     * 批量预加载会员等级数据
     *
     * @param memberMap 会员映射
     * @return 等级ID -> 等级的映射
     */
    private java.util.Map<Long, MemberLevel> preloadMemberLevels(java.util.Map<Long, Member> memberMap) {
        // 收集所有等级ID
        List<Long> levelIds = memberMap.values().stream()
                .map(Member::getLevelId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (levelIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 批量查询会员等级
        List<MemberLevel> levels = memberLevelMapper.selectBatchIds(levelIds);
        return levels.stream().collect(Collectors.toMap(MemberLevel::getId, l -> l, (a, b) -> a));
    }

    /**
     * 判断桌台是否已超时
     *
     * @param response 桌台响应信息
     * @return true-已超时 false-未超时
     */
    private boolean isOvertime(TableInfoResponse response) {
        // 只有使用中和暂停状态才能超时
        if (!"using".equals(response.getStatus()) && !"paused".equals(response.getStatus())) {
            return false;
        }
        // 不设时长的不会超时
        if (response.getPresetDuration() == null) {
            return false;
        }
        // 计算结束时间
        long pauseDuration = response.getPauseDuration() != null ? response.getPauseDuration() : 0L;
        long endTime = response.getStartTime() + response.getPresetDuration() * 1000L + pauseDuration * 1000L;
        return System.currentTimeMillis() > endTime;
    }

    /**
     * 判断桌台是否会在指定时间内到期
     *
     * @param response 桌台响应信息
     * @param currentTime 当前时间（毫秒）
     * @param remainingMillis 剩余时间毫秒数
     * @return true-会在指定时间内到期 false-不会
     */
    private boolean willExpireWithin(TableInfoResponse response, long currentTime, long remainingMillis) {
        // 只有使用中和暂停状态才会到期
        if (!"using".equals(response.getStatus()) && !"paused".equals(response.getStatus())) {
            return false;
        }
        // 不设时长的不会到期
        if (response.getPresetDuration() == null) {
            return false;
        }
        // 计算结束时间
        long pauseDuration = response.getPauseDuration() != null ? response.getPauseDuration() : 0L;
        long endTime = response.getStartTime() + response.getPresetDuration() * 1000L + pauseDuration * 1000L;
        // 计算剩余时间
        long remainingTime = endTime - currentTime;
        // 剩余时间在指定范围内（大于0且小于等于指定时间）
        return remainingTime > 0 && remainingTime <= remainingMillis;
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
        String discountName = null;
        BigDecimal discountRate = null;
        double totalDiscountAmount = 0.0;
        double activityDiscountAmount = 0.0;
        double memberDiscountAmount = 0.0;

        // 不限时套餐：获取不限时套餐价格
        if (request.getUnlimited() != null && request.getUnlimited()) {
            String channel = request.getChannel() != null ? request.getChannel() : TableConstants.Channel.STORE;
            Double unlimitedPrice = billingService.getUnlimitedPrice(channel);
            if (unlimitedPrice != null) {
                originalAmount = unlimitedPrice;
                finalAmount = originalAmount;
                log.info("使用不限时套餐价格: channel={}, unlimitedPrice={}", channel, unlimitedPrice);
            }
        }
        // 限时套餐：按时长计算价格
        else if (presetDuration != null) {
            String channel = request.getChannel() != null ? request.getChannel() : TableConstants.Channel.STORE;
            AmountDetail amountDetail = billingService.calculateAmount(channel, presetDuration, presetDuration);
            originalAmount = amountDetail.getTotalAmount();
            finalAmount = originalAmount;

            log.info("使用渠道 {} 计算初始费用: presetDuration={}秒, originalAmount={}", channel, presetDuration, originalAmount);

            // 叠加折扣逻辑：先应用会员折扣，再应用活动折扣
            double amountAfterMemberDiscount = originalAmount;
            String memberDiscountName = null;
            BigDecimal memberDiscountRateValue = null;

            // 步骤1：应用会员折扣
            if (request.getMemberId() != null) {
                amountAfterMemberDiscount = memberDiscountService.applyDiscount(request.getMemberId(), originalAmount);
                // 保留两位小数，四舍五入
                amountAfterMemberDiscount = Math.round(amountAfterMemberDiscount * 100.0) / 100.0;
                // 获取会员折扣信息
                try {
                    Member member = memberMapper.selectById(request.getMemberId());
                    if (member != null && member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelMapper.selectById(member.getLevelId());
                        if (memberLevel != null) {
                            memberDiscountName = memberLevel.getName();
                            memberDiscountRateValue = memberLevel.getDiscountRate();
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取会员折扣信息失败: {}", e.getMessage());
                }
                log.info("应用会员折扣: originalAmount={}, amountAfterMemberDiscount={}, memberDiscountName={}, memberDiscountRate={}",
                        originalAmount, amountAfterMemberDiscount, memberDiscountName, memberDiscountRateValue);
            }

            // 步骤2：应用活动折扣（在会员折扣后的价格基础上）
            if (request.getDiscountId() != null && !request.getDiscountId().isEmpty()) {
                discountId = request.getDiscountId();
                // 根据折扣ID计算折扣（基于会员折扣后的价格）
                CalculateDiscountResponse discountResponse = discountService.calculateDiscountById(
                        request.getDiscountId(),
                        BigDecimal.valueOf(amountAfterMemberDiscount),
                        request.getMemberId()
                );
                if (discountResponse != null && discountResponse.getFinalAmount() != null) {
                    discountName = discountResponse.getAppliedDiscountName();
                    discountRate = discountResponse.getDiscountRate();
                    finalAmount = discountResponse.getFinalAmount().doubleValue();
                    // 保留两位小数，四舍五入
                    finalAmount = Math.round(finalAmount * 100.0) / 100.0;
                    // 计算各项折扣金额，保留两位小数
                    totalDiscountAmount = Math.round((originalAmount - finalAmount) * 100.0) / 100.0;
                    activityDiscountAmount = Math.round((amountAfterMemberDiscount - finalAmount) * 100.0) / 100.0;
                    memberDiscountAmount = Math.round((originalAmount - amountAfterMemberDiscount) * 100.0) / 100.0;

                    // 组合折扣名称
                    if (memberDiscountName != null) {
                        discountName = memberDiscountName + "+" + discountResponse.getAppliedDiscountName();
                    }

                    log.info("应用活动折扣（叠加）: amountAfterMemberDiscount={}, finalAmount={}, discountRate={}, discountName={}, totalDiscountAmount={}, memberDiscountAmount={}, activityDiscountAmount={}",
                            amountAfterMemberDiscount, finalAmount, discountRate, discountName, totalDiscountAmount, memberDiscountAmount, activityDiscountAmount);
                } else {
                    // 活动折扣应用失败，使用会员折扣后的价格
                    finalAmount = amountAfterMemberDiscount;
                    totalDiscountAmount = Math.round((originalAmount - amountAfterMemberDiscount) * 100.0) / 100.0;
                    memberDiscountAmount = totalDiscountAmount;
                    discountName = memberDiscountName;
                    discountRate = memberDiscountRateValue;
                    log.info("活动折扣应用失败，使用会员折扣: finalAmount={}, totalDiscountAmount={}", finalAmount, totalDiscountAmount);
                }
            } else {
                // 没有活动折扣，使用会员折扣后的价格
                finalAmount = amountAfterMemberDiscount;
                totalDiscountAmount = Math.round((originalAmount - amountAfterMemberDiscount) * 100.0) / 100.0;
                memberDiscountAmount = totalDiscountAmount;
                discountName = memberDiscountName;
                discountRate = memberDiscountRateValue;
                log.info("仅应用会员折扣: finalAmount={}, totalDiscountAmount={}", finalAmount, totalDiscountAmount);
            }
        }

        return new AmountCalculationResult(originalAmount, finalAmount, discountId, discountName, discountRate,
                totalDiscountAmount, activityDiscountAmount, memberDiscountAmount);
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

        // 保存活动折扣信息
        order.setDiscountId(amountCalc.getDiscountId());
        order.setDiscountName(amountCalc.getDiscountName());
        order.setDiscountRate(amountCalc.getDiscountRate());
        order.setDiscountAmount(BigDecimal.valueOf(amountCalc.getActivityDiscountAmount()));
        order.setMemberDiscount(BigDecimal.valueOf(amountCalc.getMemberDiscountAmount()));

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

        // 1. 验证桌台和订单
        Table table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException(ErrorCode.TABLE_NOT_FOUND);
        }

        if (!TableConstants.canExtend(table.getStatus())) {
            throw new BusinessException(ErrorCode.TABLE_STATUS_ERROR);
        }

        Order currentOrder = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getId, table.getCurrentOrderId())
        );
        if (currentOrder == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2. 计算续费金额
        ExtendCalculationResult extendCalc = calculateExtendAmount(currentOrder, request);

        // 3. 判断是否为首次续费（通过查询是否存在子订单）
        boolean isFirstExtend = isFirstExtend(currentOrder);

        if (isFirstExtend) {
            // 首次续费：重构为父子订单结构
            log.info("首次续费，开始重构订单结构: orderId={}", currentOrder.getId());
            handleFirstExtend(currentOrder, table, request, extendCalc, userId, username);
        } else {
            // 后续续费：创建新的子订单
            log.info("后续续费，创建新的子订单: orderId={}", currentOrder.getId());
            handleSubsequentExtend(currentOrder, table, request, extendCalc, userId, username);
        }

        // 4. 推送状态变更事件
        eventPublisher.publishEvent(new TableStatusChangeEvent(this, tableId));

        log.info("续费时长成功: tableId={}", tableId);

        return convertToResponse(table);
    }

    /**
     * 计算续费金额
     * 注意：返回的金额只是本次续费的实际金额，不包含原订单金额
     */
    private ExtendCalculationResult calculateExtendAmount(Order order, ExtendTableRequest request) {
        String extendChannel = request.getChannel() != null ? request.getChannel() : order.getChannel();
        if (extendChannel == null) {
            extendChannel = TableConstants.Channel.STORE;
        }

        double extendFee = 0.0;

        // 如果续费时长为0，说明是选择不限时套餐
        if (request.getAdditionalDuration() == 0) {
            // 获取不限时套餐价格
            Double unlimitedPrice = billingService.getUnlimitedPrice(extendChannel);
            if (unlimitedPrice != null) {
                // 如果当前订单已经是不限时套餐，续费不限时套餐价格为0（无意义）
                if (order.getPresetDuration() == null) {
                    log.warn("当前已是不限时套餐，无需续费不限时套餐");
                    extendFee = 0.0;
                } else {
                    // 从限时套餐改为不限时套餐：用不限时价格减去当前价格
                    double currentAmount = order.getOriginalAmount() != null ? order.getOriginalAmount().doubleValue() : 0.0;
                    extendFee = Math.max(0, unlimitedPrice - currentAmount);
                    log.info("从限时套餐改为不限时套餐: currentAmount={}, unlimitedPrice={}, extendFee={}",
                            currentAmount, unlimitedPrice, extendFee);
                }
            } else {
                log.warn("未找到不限时套餐价格，续费时长为0时无法计算费用");
                extendFee = 0.0;
            }
        } else {
            // 普通续费：按时长计算
            AmountDetail extendAmountDetail = billingService.calculateAmount(
                    extendChannel,
                    request.getAdditionalDuration(),
                    request.getAdditionalDuration()
            );
            extendFee = extendAmountDetail.getTotalAmount();
            log.info("使用渠道 {} 计算续费费用: additionalDuration={}秒, extendFee={}", extendChannel, request.getAdditionalDuration(), extendFee);
        }

        // 计算本次续费折扣（基于本次续费的原价extendFee）
        ExtendDiscountCalculation discountCalc = calculateExtendDiscount(request, order, extendFee);
        double finalAmount = discountCalc.getFinalAmount();

        Integer currentPresetDuration = order.getPresetDuration();
        Integer newPresetDuration = currentPresetDuration == null
                ? (request.getAdditionalDuration() == 0 ? null : request.getAdditionalDuration())
                : (request.getAdditionalDuration() == 0 ? null : currentPresetDuration + request.getAdditionalDuration());

        // 返回本次续费的实际金额，不累加原订单金额
        return new ExtendCalculationResult(extendFee, finalAmount, newPresetDuration,
                discountCalc.getDiscountId(), discountCalc.getDiscountName(),
                discountCalc.getDiscountRate(), discountCalc.getDiscountAmount(),
                discountCalc.getActivityDiscountAmount(), discountCalc.getMemberDiscountAmount());
    }

    /**
     * 续费折扣计算结果
     */
    private static class ExtendDiscountCalculation {
        private final double finalAmount;
        private final String discountId;
        private final String discountName;
        private final BigDecimal discountRate;
        private final double totalDiscountAmount;      // 总折扣金额（会员折扣+活动折扣）
        private final double activityDiscountAmount;    // 活动折扣金额
        private final double memberDiscountAmount;      // 会员折扣金额

        public ExtendDiscountCalculation(double finalAmount, String discountId, String discountName,
                                         BigDecimal discountRate, double totalDiscountAmount,
                                         double activityDiscountAmount, double memberDiscountAmount) {
            this.finalAmount = finalAmount;
            this.discountId = discountId;
            this.discountName = discountName;
            this.discountRate = discountRate;
            this.totalDiscountAmount = totalDiscountAmount;
            this.activityDiscountAmount = activityDiscountAmount;
            this.memberDiscountAmount = memberDiscountAmount;
        }

        public double getFinalAmount() {
            return finalAmount;
        }

        public String getDiscountId() {
            return discountId;
        }

        public String getDiscountName() {
            return discountName;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }

        public double getDiscountAmount() {
            return totalDiscountAmount;
        }

        public double getActivityDiscountAmount() {
            return activityDiscountAmount;
        }

        public double getMemberDiscountAmount() {
            return memberDiscountAmount;
        }
    }

    /**
     * 计算续费折扣
     */
    private ExtendDiscountCalculation calculateExtendDiscount(ExtendTableRequest request, Order order, double newOriginalAmount) {
        // 叠加折扣逻辑：先应用会员折扣，再应用活动折扣
        double amountAfterMemberDiscount = newOriginalAmount;
        String memberDiscountName = null;
        BigDecimal memberDiscountRate = null;

        // 步骤1：应用会员折扣
        if (request.getMemberId() != null) {
            amountAfterMemberDiscount = memberDiscountService.applyDiscount(request.getMemberId(), newOriginalAmount);
            // 保留两位小数，四舍五入
            amountAfterMemberDiscount = Math.round(amountAfterMemberDiscount * 100.0) / 100.0;
            // 获取会员折扣信息
            try {
                Member member = memberMapper.selectById(request.getMemberId());
                if (member != null && member.getLevelId() != null) {
                    MemberLevel memberLevel = memberLevelMapper.selectById(member.getLevelId());
                    if (memberLevel != null) {
                        memberDiscountName = memberLevel.getName();
                        memberDiscountRate = memberLevel.getDiscountRate();
                    }
                }
            } catch (Exception e) {
                log.warn("获取会员折扣信息失败: {}", e.getMessage());
            }
            log.info("续费应用会员折扣: originalAmount={}, amountAfterMemberDiscount={}, memberDiscountName={}, memberDiscountRate={}",
                    newOriginalAmount, amountAfterMemberDiscount, memberDiscountName, memberDiscountRate);
        }

        // 步骤2：应用活动折扣（在会员折扣后的价格基础上）
        if (request.getDiscountId() != null && !request.getDiscountId().isEmpty()) {
            // 续费折扣计算：基于会员折扣后的价格
            CalculateDiscountResponse discountResponse = discountService.calculateDiscountById(
                    request.getDiscountId(),
                    BigDecimal.valueOf(amountAfterMemberDiscount),
                    request.getMemberId()
            );
            if (discountResponse != null && discountResponse.getFinalAmount() != null) {
                double finalAmount = discountResponse.getFinalAmount().doubleValue();
                // 保留两位小数，四舍五入
                finalAmount = Math.round(finalAmount * 100.0) / 100.0;
                BigDecimal discountRate = discountResponse.getDiscountRate();
                String discountName = discountResponse.getAppliedDiscountName();
                // 计算各项折扣金额，保留两位小数
                double totalDiscountAmount = Math.round((newOriginalAmount - finalAmount) * 100.0) / 100.0;
                double activityDiscountAmount = Math.round((amountAfterMemberDiscount - finalAmount) * 100.0) / 100.0;
                double memberDiscountAmount = Math.round((newOriginalAmount - amountAfterMemberDiscount) * 100.0) / 100.0;

                log.info("续费应用活动折扣（叠加）: amountAfterMemberDiscount={}, finalAmount={}, discountRate={}, discountName={}, totalDiscountAmount={}, memberDiscountAmount={}, activityDiscountAmount={}",
                        amountAfterMemberDiscount, finalAmount, discountRate, discountName, totalDiscountAmount, memberDiscountAmount, activityDiscountAmount);

                // 返回活动折扣信息，同时记录会员折扣信息到折扣名称中
                String combinedDiscountName = discountName;
                if (memberDiscountName != null) {
                    combinedDiscountName = memberDiscountName + "+" + discountName;
                }

                return new ExtendDiscountCalculation(finalAmount, request.getDiscountId(), combinedDiscountName, discountRate, totalDiscountAmount, activityDiscountAmount, memberDiscountAmount);
            }
        }

        // 没有活动折扣，使用会员折扣后的价格
        if (request.getMemberId() != null) {
            // 有会员折扣但无活动折扣，返回会员折扣信息
            double totalDiscountAmount = Math.round((newOriginalAmount - amountAfterMemberDiscount) * 100.0) / 100.0;
            log.info("续费仅应用会员折扣: finalAmount={}, totalDiscountAmount={}", amountAfterMemberDiscount, totalDiscountAmount);
            return new ExtendDiscountCalculation(amountAfterMemberDiscount, null, memberDiscountName, memberDiscountRate, totalDiscountAmount, 0, totalDiscountAmount);
        }

        // 没有任何折扣
        return new ExtendDiscountCalculation(newOriginalAmount, null, null, null, 0, 0, 0);
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

        // 更新活动折扣信息
        order.setDiscountId(extendCalc.getDiscountId());
        order.setDiscountName(extendCalc.getDiscountName());
        order.setDiscountRate(extendCalc.getDiscountRate());
        order.setDiscountAmount(BigDecimal.valueOf(extendCalc.getActivityDiscountAmount()));
        // 累加会员折扣金额
        BigDecimal currentMemberDiscount = order.getMemberDiscount() != null ? order.getMemberDiscount() : BigDecimal.ZERO;
        BigDecimal newMemberDiscount = BigDecimal.valueOf(extendCalc.getMemberDiscountAmount());
        order.setMemberDiscount(currentMemberDiscount.add(newMemberDiscount));

        order.setUpdatedAt(now);
    }

    /**
     * 更新桌台（续费）
     */
    private void updateTableForExtend(Table table, Integer newPresetDuration) {
        table.setPresetDuration(newPresetDuration);
        table.setUpdatedAt(System.currentTimeMillis());
    }

    /**
     * 判断是否为首次续费
     * 规则：当前订单没有子订单则为首次续费
     *
     * @param order 当前订单
     * @return true-首次续费，false-后续续费
     */
    private boolean isFirstExtend(Order order) {
        // 查询是否存在以当前订单为父订单的子订单
        Long childCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getParentId, order.getId())
        );
        return childCount == 0;
    }

    /**
     * 查找父订单
     * 如果当前订单是子订单，返回其父订单；否则返回当前订单本身
     *
     * @param order 当前订单
     * @return 父订单
     */
    private Order findParentOrder(Order order) {
        if (order.getParentId() != null) {
            // 当前是子订单，查询父订单
            return orderMapper.selectById(order.getParentId());
        } else {
            // 当前就是父订单
            return order;
        }
    }

    /**
     * 构建子订单
     * 子订单记录单次续费的详细信息
     *
     * @param referenceOrder 参考订单（父订单或原订单）
     * @param request 续费请求
     * @param extendCalc 续费计算结果
     * @param userId 用户ID
     * @param username 用户名
     * @param parentId 父订单ID
     * @param now 当前时间戳
     * @return 子订单
     */
    private Order buildChildOrder(Order referenceOrder, ExtendTableRequest request, ExtendCalculationResult extendCalc,
                                  String userId, String username, String parentId, long now) {
        Order childOrder = new Order();
        childOrder.setId(IdUtil.simpleUUID());
        childOrder.setTableId(referenceOrder.getTableId());
        childOrder.setTableName(referenceOrder.getTableName());
        childOrder.setStartTime(now);
        childOrder.setParentId(parentId); // 设置父订单ID
        childOrder.setDuration(request.getAdditionalDuration());
        childOrder.setPresetDuration(request.getAdditionalDuration() == 0 ? null : request.getAdditionalDuration());
        childOrder.setOriginalAmount(BigDecimal.valueOf(extendCalc.getNewOriginalAmount()));
        childOrder.setAmount(BigDecimal.valueOf(extendCalc.getNewTotalAmount()));
        // 设置金额明细JSON
        childOrder.setAmountDetail(buildSimpleAmountDetailJson(extendCalc.getNewTotalAmount()));
        childOrder.setMemberId(request.getMemberId() != null ? request.getMemberId() : referenceOrder.getMemberId());
        childOrder.setChannel(request.getChannel() != null ? request.getChannel() : referenceOrder.getChannel());
        childOrder.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : referenceOrder.getPaymentMethod());
        childOrder.setStatus(TableConstants.OrderStatus.ACTIVE);
        childOrder.setOperatorId(userId);
        childOrder.setOperatorName(username);

        // 保存活动折扣信息
        childOrder.setDiscountId(extendCalc.getDiscountId());
        childOrder.setDiscountName(extendCalc.getDiscountName());
        childOrder.setDiscountRate(extendCalc.getDiscountRate());
        childOrder.setDiscountAmount(BigDecimal.valueOf(extendCalc.getActivityDiscountAmount()));
        // 保存会员折扣金额
        childOrder.setMemberDiscount(BigDecimal.valueOf(extendCalc.getMemberDiscountAmount()));

        childOrder.setCreatedAt(now);
        childOrder.setUpdatedAt(now);
        return childOrder;
    }

    /**
     * 首次续费：将单订单重构为父子订单结构
     * 执行步骤：
     * 1. 创建父订单（汇总订单）
     * 2. 将原订单转为子订单1
     * 3. 创建新的子订单2（续费订单）
     * 4. 更新桌台关联到父订单
     *
     * @param originalOrder 原订单
     * @param table 桌台
     * @param request 续费请求
     * @param extendCalc 续费计算结果
     * @param userId 用户ID
     * @param username 用户名
     */
    /**
     * 首次续费：创建父子订单结构
     * 执行步骤：
     * 1. 创建子订单1（复制原订单的初始下单数据）
     * 2. 创建子订单2（续费订单的实际数据）
     * 3. 更新原订单为父订单（汇总统计，保持active状态）
     * 4. 更新桌台（current_order_id继续指向原订单）
     *
     * @param originalOrder 原订单
     * @param table 桌台
     * @param request 续费请求
     * @param extendCalc 续费计算结果
     * @param userId 用户ID
     * @param username 用户名
     */
    private void handleFirstExtend(Order originalOrder, Table table, ExtendTableRequest request,
                                   ExtendCalculationResult extendCalc, String userId, String username) {
        long now = System.currentTimeMillis();

        // Step 1: 创建子订单1（复制原订单的初始下单数据）
        Order childOrder1 = buildChildOrderFromOriginal(originalOrder, originalOrder.getId(), now);
        orderMapper.insert(childOrder1);

        // Step 2: 创建子订单2（续费订单的实际数据）
        Order childOrder2 = buildChildOrder(originalOrder, request, extendCalc, userId, username, originalOrder.getId(), now);
        orderMapper.insert(childOrder2);

        // Step 3: 更新原订单为父订单（汇总统计，保持active状态）
        updateOriginalOrderToParent(originalOrder, extendCalc, now);
        orderMapper.updateById(originalOrder);

        // Step 4: 更新桌台（current_order_id继续指向原订单，保持不变）
        updateTableForExtend(table, extendCalc.getNewPresetDuration());
        tableMapper.updateById(table);

        // Step 5: 记录折扣信息（应用到子订单2）
        if (extendCalc.getDiscountId() != null && extendCalc.getDiscountRate() != null) {
            discountRecordService.addDiscountRecord(childOrder2.getId(), extendCalc.getDiscountId(), extendCalc.getDiscountRate());
        }

        log.info("首次续费完成，原订单转为父订单: parentOrderId={}, childOrder1Id={}, childOrder2Id={}",
                originalOrder.getId(), childOrder1.getId(), childOrder2.getId());
    }

    /**
     * 更新原订单为父订单（汇总统计）
     * 更新汇总金额和时长，状态保持active
     *
     * @param originalOrder 原订单
     * @param extendCalc 续费计算结果
     * @param now 当前时间戳
     */
    private void updateOriginalOrderToParent(Order originalOrder, ExtendCalculationResult extendCalc, long now) {
        // 计算汇总金额
        BigDecimal totalOriginalAmount = originalOrder.getOriginalAmount()
                .add(BigDecimal.valueOf(extendCalc.getNewOriginalAmount()));
        BigDecimal totalAmount = originalOrder.getAmount()
                .add(BigDecimal.valueOf(extendCalc.getNewTotalAmount()));

        // 累加会员折扣金额
        BigDecimal currentMemberDiscount = originalOrder.getMemberDiscount() != null ? originalOrder.getMemberDiscount() : BigDecimal.ZERO;
        BigDecimal newMemberDiscount = BigDecimal.valueOf(extendCalc.getMemberDiscountAmount());
        originalOrder.setMemberDiscount(currentMemberDiscount.add(newMemberDiscount));

        // 累加活动折扣金额
        BigDecimal currentDiscountAmount = originalOrder.getDiscountAmount() != null ? originalOrder.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal newDiscountAmount = BigDecimal.valueOf(extendCalc.getActivityDiscountAmount());
        originalOrder.setDiscountAmount(currentDiscountAmount.add(newDiscountAmount));

        // 更新折扣信息（使用续费的折扣信息）
        if (extendCalc.getDiscountId() != null) {
            originalOrder.setDiscountId(extendCalc.getDiscountId());
            originalOrder.setDiscountName(extendCalc.getDiscountName());
            originalOrder.setDiscountRate(extendCalc.getDiscountRate());
        }

        // 更新汇总金额
        originalOrder.setOriginalAmount(totalOriginalAmount);
        originalOrder.setAmount(totalAmount);
        originalOrder.setAmountDetail(buildSimpleAmountDetailJson(totalAmount.doubleValue()));

        // 更新预设时长
        originalOrder.setPresetDuration(extendCalc.getNewPresetDuration());

        // 状态保持active，不修改
        originalOrder.setUpdatedAt(now);
    }

    /**
     * 从原订单构建子订单（用于首次续费时）
     * 复制原订单的所有内容，但设置新的ID和parent_id
     *
     * @param originalOrder 原订单
     * @param parentId 父订单ID
     * @param now 当前时间戳
     * @return 子订单
     */
    private Order buildChildOrderFromOriginal(Order originalOrder, String parentId, long now) {
        Order childOrder = new Order();
        childOrder.setId(IdUtil.simpleUUID());
        childOrder.setTableId(originalOrder.getTableId());
        childOrder.setTableName(originalOrder.getTableName());
        childOrder.setStartTime(originalOrder.getStartTime());
        childOrder.setParentId(parentId); // 设置父订单ID
        childOrder.setEndTime(originalOrder.getEndTime());
        childOrder.setDuration(originalOrder.getDuration());
        childOrder.setPauseDuration(originalOrder.getPauseDuration());
        childOrder.setPresetDuration(originalOrder.getPresetDuration());
        childOrder.setStatus(TableConstants.OrderStatus.ACTIVE);
        childOrder.setOriginalAmount(originalOrder.getOriginalAmount());
        childOrder.setAmount(originalOrder.getAmount());
        childOrder.setAmountDetail(originalOrder.getAmountDetail());
        childOrder.setMemberId(originalOrder.getMemberId());
        childOrder.setChannel(originalOrder.getChannel());
        childOrder.setPaymentMethod(originalOrder.getPaymentMethod());
        childOrder.setBalanceAmount(originalOrder.getBalanceAmount());
        childOrder.setOtherPaymentAmount(originalOrder.getOtherPaymentAmount());
        childOrder.setOperatorId(originalOrder.getOperatorId());
        childOrder.setOperatorName(originalOrder.getOperatorName());
        // 复制折扣信息
        childOrder.setDiscountId(originalOrder.getDiscountId());
        childOrder.setDiscountName(originalOrder.getDiscountName());
        childOrder.setDiscountRate(originalOrder.getDiscountRate());
        childOrder.setDiscountAmount(originalOrder.getDiscountAmount());
        childOrder.setMemberDiscount(originalOrder.getMemberDiscount());
        childOrder.setCreatedAt(originalOrder.getCreatedAt());
        childOrder.setUpdatedAt(now);
        return childOrder;
    }

    /**
     * 后续续费：创建新的子订单并更新父订单汇总
     * 执行步骤：
     * 1. 查找父订单
     * 2. 创建新的子订单（记录当前这次续费的实际数据）
     * 3. 更新父订单的汇总金额和时长
     * 4. 更新桌台
     *
     * @param currentOrder 当前订单
     * @param table 桌台
     * @param request 续费请求
     * @param extendCalc 续费计算结果
     * @param userId 用户ID
     * @param username 用户名
     */
    private void handleSubsequentExtend(Order currentOrder, Table table, ExtendTableRequest request,
                                        ExtendCalculationResult extendCalc, String userId, String username) {
        // Step 1: 查找父订单
        Order parentOrder = findParentOrder(currentOrder);
        if (parentOrder == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "找不到父订单");
        }

        // Step 2: 创建新子订单（记录当前这次续费的实际数据）
        Order newChildOrder = buildChildOrder(parentOrder, request, extendCalc, userId, username,
                parentOrder.getId(), System.currentTimeMillis());
        orderMapper.insert(newChildOrder);

        // Step 3: 更新父订单的汇总金额和时长
        updateParentOrderSummary(parentOrder, newChildOrder, extendCalc.getNewPresetDuration());
        orderMapper.updateById(parentOrder);

        // Step 4: 更新桌台
        updateTableForExtend(table, extendCalc.getNewPresetDuration());
        tableMapper.updateById(table);

        // Step 5: 记录折扣信息
        if (extendCalc.getDiscountId() != null && extendCalc.getDiscountRate() != null) {
            discountRecordService.addDiscountRecord(newChildOrder.getId(), extendCalc.getDiscountId(), extendCalc.getDiscountRate());
        }

        log.info("后续续费完成: parentOrderId={}, newChildOrderId={}, totalAmount={}",
                parentOrder.getId(), newChildOrder.getId(), parentOrder.getAmount());
    }

    /**
     * 更新父订单的汇总金额和时长
     *
     * @param parentOrder 父订单
     * @param newChildOrder 新创建的子订单
     * @param newPresetDuration 新的预设时长
     */
    private void updateParentOrderSummary(Order parentOrder, Order newChildOrder, Integer newPresetDuration) {
        // 累加子订单的金额到父订单
        BigDecimal totalOriginalAmount = parentOrder.getOriginalAmount()
                .add(newChildOrder.getOriginalAmount());
        BigDecimal totalAmount = parentOrder.getAmount()
                .add(newChildOrder.getAmount());

        // 更新父订单的汇总金额
        parentOrder.setOriginalAmount(totalOriginalAmount);
        parentOrder.setAmount(totalAmount);
        parentOrder.setAmountDetail(buildSimpleAmountDetailJson(totalAmount.doubleValue()));

        // 更新预设时长
        parentOrder.setPresetDuration(newPresetDuration);

        parentOrder.setUpdatedAt(System.currentTimeMillis());
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

                // 同步更新所有子订单的状态
                updateChildOrdersStatus(order.getId(), order.getStatus(), now);

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
     * 同步更新所有子订单的状态
     *
     * @param parentOrderId 父订单ID
     * @param status 父订单的状态
     * @param now 当前时间戳
     */
    private void updateChildOrdersStatus(String parentOrderId, String status, long now) {
        // 查询所有子订单
        List<Order> childOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getParentId, parentOrderId)
        );

        if (childOrders == null || childOrders.isEmpty()) {
            log.info("父订单 {} 没有子订单，无需更新状态", parentOrderId);
            return;
        }

        // 批量更新子订单状态
        for (Order childOrder : childOrders) {
            childOrder.setStatus(status);
            childOrder.setEndTime(now);
            childOrder.setUpdatedAt(now);
            orderMapper.updateById(childOrder);
        }

        log.info("已更新 {} 个子订单的状态为: {}", childOrders.size(), status);
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
     * 转换为响应DTO（使用预加载的数据，批量查询优化）
     */
    private TableInfoResponse convertToResponse(Table table,
                                                   java.util.Map<String, Order> orderMap,
                                                   java.util.Map<Long, Member> memberMap,
                                                   java.util.Map<Long, MemberLevel> memberLevelMap,
                                                   int extendTimeMinutes) {
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

        // 计算到点时间（使用预加载的配置，避免重复查询）
        if (table.getStartTime() != null && table.getPresetDuration() != null) {
            long baseEndTime = table.getStartTime() + table.getPresetDuration() * 1000L;
            long endTime = baseEndTime + extendTimeMinutes * 60 * 1000L;
            response.setEndTime(endTime);
        }

        // 计算时长和费用
        if (TableConstants.isInUse(table.getStatus())) {
            fillDurationAndAmount(response, table, orderMap, memberMap, memberLevelMap);
        } else {
            response.setDuration(0L);
            response.setPauseDuration(0L);
            response.setAmount(0.0);
        }

        return response;
    }

    /**
     * 转换为响应DTO（兼容其他方法调用）
     */
    private TableInfoResponse convertToResponse(Table table,
                                                   java.util.Map<String, Order> orderMap,
                                                   java.util.Map<Long, Member> memberMap,
                                                   java.util.Map<Long, MemberLevel> memberLevelMap) {
        // 调用带参数的版本
        return convertToResponse(table, orderMap, memberMap, memberLevelMap, getExtendTimeConfig());
    }

    /**
     * 转换为响应DTO（兼容其他方法调用）
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
     * 填充时长和费用信息（使用预加载数据，批量查询优化）
     */
    private void fillDurationAndAmount(TableInfoResponse response, Table table,
                                         java.util.Map<String, Order> orderMap,
                                         java.util.Map<Long, Member> memberMap,
                                         java.util.Map<Long, MemberLevel> memberLevelMap) {
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
            Order currentOrder = orderMap.get(table.getCurrentOrderId());
            if (currentOrder != null) {
                response.setChannel(currentOrder.getChannel());

                if (currentOrder.getAmount() != null && currentOrder.getAmount().doubleValue() > 0) {
                    response.setAmount(currentOrder.getAmount().doubleValue());
                    if (currentOrder.getOriginalAmount() != null && currentOrder.getOriginalAmount().doubleValue() > 0) {
                        response.setOriginalAmount(currentOrder.getOriginalAmount().doubleValue());
                    }
                    fillOrderMemberInfo(response, currentOrder, memberMap, memberLevelMap);
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
     * 填充时长和费用信息（兼容其他方法调用）
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
     * 填充订单会员信息（使用预加载数据，批量查询优化）
     */
    private void fillOrderMemberInfo(TableInfoResponse response, Order order,
                                       java.util.Map<Long, Member> memberMap,
                                       java.util.Map<Long, MemberLevel> memberLevelMap) {
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
            Member member = memberMap.get(order.getMemberId());
            if (member != null) {
                response.setMemberName(member.getName());
                if (member.getLevelId() != null) {
                    MemberLevel memberLevel = memberLevelMap.get(member.getLevelId());
                    if (memberLevel != null && memberLevel.getDiscountRate() != null) {
                        response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                    }
                }
                if (member.getBalance() != null) {
                    response.setMemberBalance(member.getBalance().doubleValue());
                }
            }
        }
    }

    /**
     * 填充订单会员信息（兼容其他方法调用）
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
        private final String discountName;
        private final BigDecimal discountRate;
        private final double totalDiscountAmount;      // 总折扣金额
        private final double activityDiscountAmount;    // 活动折扣金额
        private final double memberDiscountAmount;      // 会员折扣金额

        public AmountCalculationResult(double originalAmount, double finalAmount, String discountId,
                                       String discountName, BigDecimal discountRate,
                                       double totalDiscountAmount, double activityDiscountAmount, double memberDiscountAmount) {
            this.originalAmount = originalAmount;
            this.finalAmount = finalAmount;
            this.discountId = discountId;
            this.discountName = discountName;
            this.discountRate = discountRate;
            this.totalDiscountAmount = totalDiscountAmount;
            this.activityDiscountAmount = activityDiscountAmount;
            this.memberDiscountAmount = memberDiscountAmount;
        }

        public AmountCalculationResult(double originalAmount, double finalAmount) {
            this(originalAmount, finalAmount, null, null, null, 0, 0, 0);
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

        public String getDiscountName() {
            return discountName;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }

        public double getDiscountAmount() {
            return totalDiscountAmount;
        }

        public double getActivityDiscountAmount() {
            return activityDiscountAmount;
        }

        public double getMemberDiscountAmount() {
            return memberDiscountAmount;
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
        private final String discountName;
        private final BigDecimal discountRate;
        private final double totalDiscountAmount;      // 总折扣金额
        private final double activityDiscountAmount;    // 活动折扣金额
        private final double memberDiscountAmount;      // 会员折扣金额

        public ExtendCalculationResult(double newOriginalAmount, double newTotalAmount, Integer newPresetDuration,
                                       String discountId, String discountName, BigDecimal discountRate,
                                       double totalDiscountAmount, double activityDiscountAmount, double memberDiscountAmount) {
            this.newOriginalAmount = newOriginalAmount;
            this.newTotalAmount = newTotalAmount;
            this.newPresetDuration = newPresetDuration;
            this.discountId = discountId;
            this.discountName = discountName;
            this.discountRate = discountRate;
            this.totalDiscountAmount = totalDiscountAmount;
            this.activityDiscountAmount = activityDiscountAmount;
            this.memberDiscountAmount = memberDiscountAmount;
        }

        public ExtendCalculationResult(double newOriginalAmount, double newTotalAmount, Integer newPresetDuration) {
            this(newOriginalAmount, newTotalAmount, newPresetDuration, null, null, null, 0, 0, 0);
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

        public String getDiscountName() {
            return discountName;
        }

        public BigDecimal getDiscountRate() {
            return discountRate;
        }

        public double getDiscountAmount() {
            return totalDiscountAmount;
        }

        public double getActivityDiscountAmount() {
            return activityDiscountAmount;
        }

        public double getMemberDiscountAmount() {
            return memberDiscountAmount;
        }
    }
}
