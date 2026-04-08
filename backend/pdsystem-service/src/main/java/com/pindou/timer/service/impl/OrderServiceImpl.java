package com.pindou.timer.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pindou.timer.common.exception.BusinessException;
import com.pindou.timer.common.result.ErrorCode;
import com.pindou.timer.common.result.PageResult;
import com.pindou.timer.constants.TableConstants;
import com.pindou.timer.dto.*;
import com.pindou.timer.entity.Member;
import com.pindou.timer.entity.MemberLevel;
import com.pindou.timer.entity.Order;
import com.pindou.timer.mapper.MemberLevelMapper;
import com.pindou.timer.mapper.MemberMapper;
import com.pindou.timer.mapper.OrderMapper;
import com.pindou.timer.service.BillingService;
import com.pindou.timer.service.MemberLevelService;
import com.pindou.timer.service.MemberService;
import com.pindou.timer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单Service实现类
 *
 * @author wuci
 * @date 2026-03-28
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private MemberMapper memberMapper;

    @Resource
    private MemberLevelMapper memberLevelMapper;

    @Resource
    private BillingService billingService;

    @Resource
    private MemberService memberService;

    @Resource
    private MemberLevelService memberLevelService;

    /**
     * 获取今天的起始时间（0点0分0秒）
     */
    private long getTodayStartTime() {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000) - 8 * 60 * 60 * 1000;
    }

    @Override
    public PageResult<OrderInfoResponse> getActiveOrders(Integer page, Integer pageSize) {
        long startTime = System.currentTimeMillis();
        log.info("获取当天已完成订单列表: page={}, pageSize={}", page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 只查询父订单（parentId为NULL）
        wrapper.isNull(Order::getParentId);
        // 查询当天已完成的订单
        wrapper.eq(Order::getStatus, "completed");
        // 查询创建时间在今天的订单
        wrapper.ge(Order::getCreatedAt, getTodayStartTime());

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> pageObj = new Page<>(page, pageSize);
        Page<Order> resultPage = orderMapper.selectPage(pageObj, wrapper);

        // 批量预加载数据，优化性能（包括父订单和子订单的会员数据）
        List<Order> orders = resultPage.getRecords();
        java.util.Map<Long, Member> memberMap = preloadMembersWithChildren(orders);
        java.util.Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);

        // 转换结果
        List<OrderInfoResponse> records = orders.stream()
                .map(order -> convertToInfoResponse(order, memberMap, memberLevelMap))
                .collect(Collectors.toList());

        // 使用 PageResult.of 构建分页结果
        PageResult<OrderInfoResponse> pageResult = PageResult.of(
                records,
                resultPage.getTotal(),
                page,
                pageSize
        );

        long endTime = System.currentTimeMillis();
        log.info("获取当天已完成订单列表成功: total={}, totalPages={},耗时={}ms",
                pageResult.getTotal(), pageResult.getTotalPages(), endTime - startTime);
        return pageResult;
    }

    @Override
    public PageResult<OrderInfoResponse> getActiveOrdersNow(Integer page, Integer pageSize) {
        long startTime = System.currentTimeMillis();
        log.info("获取进行中订单列表: page={}, pageSize={}", page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 只查询父订单（parentId为NULL）
        wrapper.isNull(Order::getParentId);
        // 查询进行中的订单
        wrapper.eq(Order::getStatus, TableConstants.OrderStatus.ACTIVE);

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> pageObj = new Page<>(page, pageSize);
        Page<Order> resultPage = orderMapper.selectPage(pageObj, wrapper);

        // 批量预加载数据，优化性能（包括父订单和子订单的会员数据）
        List<Order> orders = resultPage.getRecords();
        java.util.Map<Long, Member> memberMap = preloadMembersWithChildren(orders);
        java.util.Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);

        // 转换结果
        List<OrderInfoResponse> records = orders.stream()
                .map(order -> convertToInfoResponse(order, memberMap, memberLevelMap))
                .collect(Collectors.toList());

        // 使用 PageResult.of 构建分页结果
        PageResult<OrderInfoResponse> pageResult = PageResult.of(
                records,
                resultPage.getTotal(),
                page,
                pageSize
        );

        long endTime = System.currentTimeMillis();
        log.info("获取进行中订单列表成功: total={}, totalPages={},耗时={}ms",
                pageResult.getTotal(), pageResult.getTotalPages(), endTime - startTime);
        return pageResult;
    }

    /**
     * 批量预加载会员数据
     *
     * @param orders 订单列表
     * @return 会员ID -> 会员的映射
     */
    private java.util.Map<Long, Member> preloadMembers(List<Order> orders) {
        // 收集所有会员ID
        List<Long> memberIds = orders.stream()
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
     * 批量预加载会员数据（包括子订单）
     *
     * @param parentOrders 父订单列表
     * @return 会员ID -> 会员的映射
     */
    private java.util.Map<Long, Member> preloadMembersWithChildren(List<Order> parentOrders) {
        // 收集所有会员ID（包括父订单和子订单）
        java.util.Set<Long> memberIds = new java.util.HashSet<>();

        // 收集父订单的会员ID
        parentOrders.stream()
                .filter(order -> order != null)
                .map(Order::getMemberId)
                .filter(id -> id != null)
                .forEach(memberIds::add);

        // 收集子订单的会员ID
        for (Order parentOrder : parentOrders) {
            if (parentOrder == null || parentOrder.getId() == null) {
                continue;
            }
            List<Order> childOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getParentId, parentOrder.getId())
                            .select(Order::getMemberId)  // 只查询会员ID字段，优化性能
            );
            if (childOrders != null) {
                childOrders.stream()
                        .filter(child -> child != null)
                        .map(Order::getMemberId)
                        .filter(id -> id != null)
                        .forEach(memberIds::add);
            }
        }

        if (memberIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        // 批量查询会员
        List<Member> members = memberMapper.selectBatchIds(new java.util.ArrayList<>(memberIds));
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

    @Override
    public PageResult<OrderInfoResponse> getHistoryOrders(OrderQueryRequest request) {
        log.info("获取历史订单列表: request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 只查询父订单（parentId为NULL）
        wrapper.isNull(Order::getParentId);

        // 状态筛选：
        // 1. 如果指定了状态，按指定状态查询
        // 2. 如果未指定状态或选择"all"，排除进行中的订单（只查询历史订单）
        if (StringUtils.isNotBlank(request.getStatus()) && !"all".equals(request.getStatus())) {
            wrapper.eq(Order::getStatus, request.getStatus());
        } else {
            // 排除进行中的订单
            wrapper.ne(Order::getStatus, TableConstants.OrderStatus.ACTIVE);
        }

        // 桌台筛选
        if (request.getTableId() != null) {
            wrapper.eq(Order::getTableId, request.getTableId());
        }

        // 时间范围筛选
        if (request.getStartTime() != null) {
            wrapper.ge(Order::getStartTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(Order::getStartTime, request.getEndTime());
        }

        // 关键词搜索（桌台名称/操作员）
        if (StringUtils.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword();
            wrapper.and(w -> w.like(Order::getTableName, keyword)
                    .or()
                    .like(Order::getOperatorName, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        // 分页查询
        Page<Order> page = new Page<>(request.getPage(), request.getPageSize());
        Page<Order> resultPage = orderMapper.selectPage(page, wrapper);

        // 批量预加载数据，优化性能（包括父订单和子订单的会员数据）
        List<Order> orders = resultPage.getRecords();
        java.util.Map<Long, Member> memberMap = preloadMembersWithChildren(orders);
        java.util.Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);

        // 转换结果
        List<OrderInfoResponse> records = orders.stream()
                .map(order -> convertToInfoResponse(order, memberMap, memberLevelMap))
                .collect(Collectors.toList());

        // 使用 PageResult.of 构建分页结果
        PageResult<OrderInfoResponse> pageResult = PageResult.of(
                records,
                resultPage.getTotal(),
                request.getPage(),
                request.getPageSize()
        );

        log.info("获取历史订单列表成功: total={}, totalPages={}", pageResult.getTotal(), pageResult.getTotalPages());
        return pageResult;
    }

    @Override
    public OrderDetailResponse getOrderDetail(String orderId) {
        log.info("获取订单详情: orderId={}", orderId);

        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 转换为详情响应
        OrderDetailResponse response = convertToDetailResponse(order);

        log.info("获取订单详情成功: orderId={}", orderId);
        return response;
    }

    @Override
    public List<OrderInfoResponse> exportOrders(OrderQueryRequest request) {
        log.info("导出订单: request={}", request);

        // 设置较大的分页用于导出
        request.setPage(1);
        request.setPageSize(50000);

        PageResult<OrderInfoResponse> pageResult = getHistoryOrders(request);

        log.info("导出订单成功: count={}", pageResult.getList().size());
        return pageResult.getList();
    }

    /**
     * 转换为订单信息响应（列表用，使用预加载数据）
     */
    private OrderInfoResponse convertToInfoResponse(Order order,
                                                     java.util.Map<Long, Member> memberMap,
                                                     java.util.Map<Long, MemberLevel> memberLevelMap) {
        OrderInfoResponse response = new OrderInfoResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getId());
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());

        // 计算使用时长：进行中的订单需要动态计算
        Integer duration = order.getDuration();
        if (TableConstants.OrderStatus.ACTIVE.equals(order.getStatus())) {
            // 进行中的订单：使用时长 = 当前时间 - 开始时间 - 暂停时长
            long currentTime = System.currentTimeMillis();
            long startTime = order.getStartTime() != null ? order.getStartTime() : currentTime;
            int pauseDuration = order.getPauseDuration() != null ? order.getPauseDuration() : 0;
            duration = (int) ((currentTime - startTime) / 1000) - pauseDuration;
            // 确保使用时长不为负数
            if (duration < 0) {
                duration = 0;
            }
        }
        response.setDuration(duration);
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setChannel(order.getChannel());
        response.setStatus(order.getStatus());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setParentId(order.getParentId());

        // 查询子订单
        List<Order> childOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getParentId, order.getId())
                        .orderByAsc(Order::getCreatedAt)
        );

        // 设置续费次数（子订单数量减1，因为第一个子订单是初始订单）
        if (childOrders != null && !childOrders.isEmpty()) {
            response.setExtendCount(childOrders.size() - 1);
            // 转换子订单列表
            List<OrderInfoResponse> childOrderResponses = new ArrayList<>();
            for (Order childOrder : childOrders) {
                OrderInfoResponse childResponse = convertToInfoResponse(childOrder, memberMap, memberLevelMap);
                childOrderResponses.add(childResponse);
            }
            response.setChildOrders(childOrderResponses);
        } else {
            response.setExtendCount(0);
        }

        // 设置会员ID
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
        }

        // 优先使用订单中存储的金额
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
                double discountAmount = order.getOriginalAmount().doubleValue() - order.getAmount().doubleValue();
                if (discountAmount > 0) {
                    response.setDiscountAmount(discountAmount);
                }
            }
        } else {
            // 计算当前费用
            int actualDuration = order.getDuration() - order.getPauseDuration();
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
            response.setAmount(amountDetail.getTotalAmount());
        }

        // 填充会员信息（使用预加载数据）
        if (order.getMemberId() != null) {
            Member member = memberMap.get(order.getMemberId());
            if (member != null) {
                response.setMemberName(member.getName());
                if (member.getLevelId() != null) {
                    MemberLevel memberLevel = memberLevelMap.get(member.getLevelId());
                    if (memberLevel != null) {
                        response.setMemberLevelName(memberLevel.getName());
                        if (memberLevel.getDiscountRate() != null) {
                            response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                        }
                    }
                }
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

        return response;
    }

    /**
     * 转换为订单信息响应（列表用，兼容其他方法调用）
     */
    private OrderInfoResponse convertToInfoResponse(Order order) {
        OrderInfoResponse response = new OrderInfoResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getId()); // 订单编号使用ID
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());

        // 计算使用时长：进行中的订单需要动态计算
        Integer duration = order.getDuration();
        if (TableConstants.OrderStatus.ACTIVE.equals(order.getStatus())) {
            // 进行中的订单：使用时长 = 当前时间 - 开始时间 - 暂停时长
            long currentTime = System.currentTimeMillis();
            long startTime = order.getStartTime() != null ? order.getStartTime() : currentTime;
            int pauseDuration = order.getPauseDuration() != null ? order.getPauseDuration() : 0;
            duration = (int) ((currentTime - startTime) / 1000) - pauseDuration;
            // 确保使用时长不为负数
            if (duration < 0) {
                duration = 0;
            }
        }
        response.setDuration(duration);
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setChannel(order.getChannel());
        response.setStatus(order.getStatus());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setParentId(order.getParentId());

        // 查询子订单（仅父订单需要查询）
        if (order.getParentId() == null) {
            List<Order> childOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getParentId, order.getId())
                            .orderByAsc(Order::getCreatedAt)
            );

            // 设置续费次数（子订单数量减1，因为第一个子订单是初始订单）
            if (childOrders != null && !childOrders.isEmpty()) {
                response.setExtendCount(childOrders.size() - 1);
                // 转换子订单列表
                List<OrderInfoResponse> childOrderResponses = new ArrayList<>();
                for (Order childOrder : childOrders) {
                    OrderInfoResponse childResponse = convertToInfoResponse(childOrder);
                    childOrderResponses.add(childResponse);
                }
                response.setChildOrders(childOrderResponses);
            } else {
                response.setExtendCount(0);
            }
        }

        // 调试日志：打印订单的所有字段
        log.info("转换订单信息: orderId={}, memberId={}, originalAmount={}, amount={}",
                order.getId(), order.getMemberId(), order.getOriginalAmount(), order.getAmount());

        // 设置会员ID
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
        }

        // 优先使用订单中存储的金额，如果没有则重新计算
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());

            // 设置原价
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
                // 计算折扣金额
                double discountAmount = order.getOriginalAmount().doubleValue() - order.getAmount().doubleValue();
                if (discountAmount > 0) {
                    response.setDiscountAmount(discountAmount);
                }
            }

            log.info("使用订单存储金额: orderId={}, amount={}", order.getId(), order.getAmount().doubleValue());
        } else {
            // 计算当前费用，使用订单中存储的渠道
            int actualDuration = order.getDuration() - order.getPauseDuration();
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
            response.setAmount(amountDetail.getTotalAmount());
            log.info("重新计算订单金额: orderId={}, channel={}, amount={}", order.getId(), orderChannel, amountDetail.getTotalAmount());
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            try {
                log.info("查询会员信息: orderId={}, memberId={}", order.getId(), order.getMemberId());
                Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    response.setMemberName(member.getName());
                    log.info("查询到会员: orderId={}, memberId={}, memberName={}", order.getId(), order.getMemberId(), member.getName());

                    // 获取会员等级信息
                    if (member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                        if (memberLevel != null) {
                            response.setMemberLevelName(memberLevel.getName());
                            if (memberLevel.getDiscountRate() != null) {
                                response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                            }
                            log.info("查询到会员等级: orderId={}, levelName={}, discountRate={}", order.getId(), memberLevel.getName(), memberLevel.getDiscountRate());
                        }
                    }
                } else {
                    log.warn("会员不存在: orderId={}, memberId={}", order.getId(), order.getMemberId());
                }
            } catch (Exception e) {
                log.error("获取会员信息异常: orderId={}, memberId={}, error={}", order.getId(), order.getMemberId(), e.getMessage(), e);
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

        return response;
    }

    /**
     * 转换为订单详情响应
     */
    private OrderDetailResponse convertToDetailResponse(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setChannel(order.getChannel());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());
        response.setPaidAt(order.getPaidAt());

        // 计算使用时长：进行中的订单需要动态计算
        Integer duration = order.getDuration();
        if (TableConstants.OrderStatus.ACTIVE.equals(order.getStatus())) {
            // 进行中的订单：使用时长 = 当前时间 - 开始时间 - 暂停时长
            long currentTime = System.currentTimeMillis();
            long startTime = order.getStartTime() != null ? order.getStartTime() : currentTime;
            int pauseDuration = order.getPauseDuration() != null ? order.getPauseDuration() : 0;
            duration = (int) ((currentTime - startTime) / 1000) - pauseDuration;
            // 确保使用时长不为负数
            if (duration < 0) {
                duration = 0;
            }
        }
        response.setDuration(duration);
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setStatus(order.getStatus());
        response.setOperatorId(order.getOperatorId());
        response.setOperatorName(order.getOperatorName());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // 计算计费时长
        int actualDuration = duration - (order.getPauseDuration() != null ? order.getPauseDuration() : 0);
        response.setActualDuration(actualDuration);

        // 优先使用订单中存储的金额和金额明细，如果没有则重新计算
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());

            // 设置原价
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
            }

            // 尝试解析金额明细JSON
            if (StringUtils.isNotBlank(order.getAmountDetail())) {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(order.getAmountDetail());
                    response.setNormalAmount(jsonObject.getDouble("normalAmount"));
                    response.setOvertimeAmount(jsonObject.getDouble("overtimeAmount"));

                    // 构建AmountDetail对象
                    AmountDetail amountDetail = new AmountDetail();
                    amountDetail.setTotalAmount(order.getAmount().doubleValue());
                    amountDetail.setNormalAmount(response.getNormalAmount());
                    amountDetail.setOvertimeAmount(response.getOvertimeAmount());
                    amountDetail.setActualDuration(actualDuration);
                    amountDetail.setBillingType(jsonObject.getStr("billingType"));
                    amountDetail.setUnitPrice(jsonObject.getDouble("unitPrice"));
                    amountDetail.setOvertimeRate(jsonObject.getDouble("overtimeRate"));
                    response.setAmountDetailInfo(amountDetail);

                    log.info("使用订单存储的金额明细: orderId={}, amount={}", order.getId(), order.getAmount().doubleValue());
                } catch (Exception e) {
                    log.warn("解析金额明细失败，重新计算: orderId={}, amountDetail={}", order.getId(), order.getAmountDetail());
                    // 解析失败，重新计算，使用订单中存储的渠道
                    String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
                    AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
                    response.setNormalAmount(amountDetail.getNormalAmount());
                    response.setOvertimeAmount(amountDetail.getOvertimeAmount());
                    response.setAmountDetailInfo(amountDetail);
                    log.info("使用渠道 {} 重新计算订单金额明细: orderId={}, amount={}", orderChannel, order.getId(), amountDetail.getTotalAmount());
                }
            } else {
                // 没有金额明细，使用订单金额创建简单的明细
                AmountDetail amountDetail = new AmountDetail();
                amountDetail.setTotalAmount(order.getAmount().doubleValue());
                amountDetail.setNormalAmount(order.getAmount().doubleValue());
                amountDetail.setOvertimeAmount(0.0);
                amountDetail.setActualDuration(actualDuration);
                amountDetail.setBillingType("preset");
                amountDetail.setUnitPrice(order.getAmount().doubleValue());
                amountDetail.setOvertimeRate(1.0);
                response.setNormalAmount(order.getAmount().doubleValue());
                response.setOvertimeAmount(0.0);
                response.setAmountDetailInfo(amountDetail);
            }
        } else {
            // 订单没有金额，重新计算，使用订单中存储的渠道
            String orderChannel = order.getChannel() != null ? order.getChannel() : "store";
            AmountDetail amountDetail = billingService.calculateAmount(orderChannel, actualDuration, order.getPresetDuration());
            response.setNormalAmount(amountDetail.getNormalAmount());
            response.setOvertimeAmount(amountDetail.getOvertimeAmount());
            response.setAmount(amountDetail.getTotalAmount());
            log.info("使用渠道 {} 重新计算订单金额: orderId={}, amount={}", orderChannel, order.getId(), amountDetail.getTotalAmount());
            response.setAmountDetailInfo(amountDetail);
            log.info("重新计算订单金额: orderId={}, amount={}", order.getId(), amountDetail.getTotalAmount());
        }

        // 填充会员信息
        if (order.getMemberId() != null) {
            try {
                Member member = memberService.getById(order.getMemberId());
                if (member != null) {
                    response.setMemberId(member.getId());
                    response.setMemberName(member.getName());

                    // 获取会员等级信息
                    if (member.getLevelId() != null) {
                        MemberLevel memberLevel = memberLevelService.getById(member.getLevelId());
                        if (memberLevel != null) {
                            response.setMemberLevelName(memberLevel.getName());
                            if (memberLevel.getDiscountRate() != null) {
                                response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取订单详情会员信息失败: orderId={}, memberId={}, error={}", order.getId(), order.getMemberId(), e.getMessage());
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

        // 设置父订单ID
        response.setParentId(order.getParentId());

        // 查询子订单（仅父订单需要查询）
        if (order.getParentId() == null) {
            List<Order> childOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                    .eq(Order::getParentId, order.getId())
                    .orderByAsc(Order::getCreatedAt)
            );

            // 设置续费次数（子订单数量减1，因为第一个子订单是初始订单）
            if (childOrders != null && !childOrders.isEmpty()) {
                response.setExtendCount(childOrders.size() - 1);

                // 预加载子订单的会员数据和会员等级数据
                java.util.Set<Long> memberIds = new java.util.HashSet<>();
                java.util.Set<Long> memberLevelIds = new java.util.HashSet<>();
                for (Order childOrder : childOrders) {
                    if (childOrder.getMemberId() != null) {
                        memberIds.add(childOrder.getMemberId());
                    }
                }

                // 批量查询会员
                java.util.Map<Long, Member> memberMap = new java.util.HashMap<>();
                if (!memberIds.isEmpty()) {
                    List<Member> members = memberService.listByIds(memberIds);
                    for (Member member : members) {
                        memberMap.put(member.getId(), member);
                        if (member.getLevelId() != null) {
                            memberLevelIds.add(member.getLevelId());
                        }
                    }
                }

                // 批量查询会员等级
                java.util.Map<Long, MemberLevel> memberLevelMap = new java.util.HashMap<>();
                if (!memberLevelIds.isEmpty()) {
                    List<MemberLevel> memberLevels = memberLevelService.listByIds(memberLevelIds);
                    for (MemberLevel memberLevel : memberLevels) {
                        memberLevelMap.put(memberLevel.getId(), memberLevel);
                    }
                }

                // 转换子订单列表
                List<OrderInfoResponse> childOrderResponses = new ArrayList<>();
                for (Order childOrder : childOrders) {
                    OrderInfoResponse childResponse = convertChildOrderToInfoResponse(childOrder, memberMap, memberLevelMap);
                    childOrderResponses.add(childResponse);
                }
                response.setChildOrders(childOrderResponses);
            } else {
                response.setExtendCount(0);
            }
        }

        // 构建时间线
        List<TimeLineItem> timeLine = buildTimeLine(order);
        response.setTimeLine(timeLine);

        return response;
    }

    /**
     * 构建时间线记录
     * 注：实际项目中应从操作日志表读取，这里先简化处理
     */
    private List<TimeLineItem> buildTimeLine(Order order) {
        List<TimeLineItem> timeLine = new ArrayList<>();

        // 开始记录
        TimeLineItem startItem = new TimeLineItem();
        startItem.setTime(order.getStartTime());
        startItem.setAction("start");
        startItem.setDescription("开始计时");
        startItem.setOperator(order.getOperatorName());
        timeLine.add(startItem);

        // 结束记录
        if (order.getEndTime() != null) {
            TimeLineItem endItem = new TimeLineItem();
            endItem.setTime(order.getEndTime());
            endItem.setAction("end");
            endItem.setDescription("结束计时并结账");
            endItem.setOperator(order.getOperatorName());
            timeLine.add(endItem);
        }

        // 支付记录
        if (order.getPaidAt() != null) {
            TimeLineItem paidItem = new TimeLineItem();
            paidItem.setTime(order.getPaidAt());
            paidItem.setAction("paid");
            paidItem.setDescription("支付完成");
            paidItem.setOperator(order.getOperatorName());
            timeLine.add(paidItem);
        }

        return timeLine;
    }

    /**
     * 转换子订单为订单信息响应（不包含子订单的子订单）
     */
    private OrderInfoResponse convertChildOrderToInfoResponse(Order order,
                                                             java.util.Map<Long, Member> memberMap,
                                                             java.util.Map<Long, MemberLevel> memberLevelMap) {
        OrderInfoResponse response = new OrderInfoResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getId());
        response.setTableId(order.getTableId());
        response.setTableName(order.getTableName());
        response.setStartTime(order.getStartTime());
        response.setEndTime(order.getEndTime());

        // 计算使用时长：进行中的订单需要动态计算
        Integer duration = order.getDuration();
        if (TableConstants.OrderStatus.ACTIVE.equals(order.getStatus())) {
            // 进行中的订单：使用时长 = 当前时间 - 开始时间 - 暂停时长
            long currentTime = System.currentTimeMillis();
            long startTime = order.getStartTime() != null ? order.getStartTime() : currentTime;
            int pauseDuration = order.getPauseDuration() != null ? order.getPauseDuration() : 0;
            duration = (int) ((currentTime - startTime) / 1000) - pauseDuration;
            // 确保使用时长不为负数
            if (duration < 0) {
                duration = 0;
            }
        }
        response.setDuration(duration);
        response.setPauseDuration(order.getPauseDuration());
        response.setPresetDuration(order.getPresetDuration());
        response.setChannel(order.getChannel());
        response.setStatus(order.getStatus());
        response.setOperatorName(order.getOperatorName());
        response.setPaidAt(order.getPaidAt());
        response.setCreatedAt(order.getCreatedAt());
        response.setParentId(order.getParentId());

        // 设置会员ID
        if (order.getMemberId() != null) {
            response.setMemberId(order.getMemberId());
        }

        // 优先使用订单中存储的金额
        if (order.getAmount() != null && order.getAmount().doubleValue() > 0) {
            response.setAmount(order.getAmount().doubleValue());

            // 设置原价
            if (order.getOriginalAmount() != null && order.getOriginalAmount().doubleValue() > 0) {
                response.setOriginalAmount(order.getOriginalAmount().doubleValue());
            }
        }

        // 设置会员信息（使用预加载的数据）
        if (order.getMemberId() != null && memberMap.containsKey(order.getMemberId())) {
            Member member = memberMap.get(order.getMemberId());
            response.setMemberName(member.getName());

            // 设置会员等级信息（使用预加载的数据）
            if (member.getLevelId() != null && memberLevelMap.containsKey(member.getLevelId())) {
                MemberLevel memberLevel = memberLevelMap.get(member.getLevelId());
                response.setMemberLevelName(memberLevel.getName());
                if (memberLevel.getDiscountRate() != null) {
                    response.setMemberDiscountRate(memberLevel.getDiscountRate().doubleValue());
                }
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

        return response;
    }
}
