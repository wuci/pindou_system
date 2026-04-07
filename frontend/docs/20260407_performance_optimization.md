# 性能优化记录

## 日期
2026-04-07

## 问题描述
以下接口响应缓慢，需要排查并优化：
1. `TableController.getTableList()` - 获取桌台列表
2. `OrderController.getActiveOrders()` - 获取当天已完成订单列表

## 问题分析

### 1. TableController.getTableList() 性能问题

**问题原因：N+1 查询问题**

每个使用中的台台在转换时会执行：
- `orderMapper.selectById()` - 查询订单
- `memberService.getById()` - 查询会员
- `memberLevelService.getById()` - 查询会员等级

**性能影响：**
- 如果有20个使用中的台台，会执行 20 + 20 + 20 = **60次数据库查询**

### 2. OrderController.getActiveOrders() 性能问题

**问题原因：N+1 查询问题**

每个订单在转换时会执行：
- `memberService.getById()` - 查询会员
- `memberLevelService.getById()` - 查询会员等级

**性能影响：**
- 如果有10个订单，会执行 10 + 10 = **20次数据库查询**

## 优化方案

### 核心思路：批量预加载 + Map 查找

**优化前（循环查询）：**
```
for each table:
    order = selectById(table.orderId)        // N次查询
    member = selectById(order.memberId)       // N次查询
    level = selectById(member.levelId)        // N次查询
```

**优化后（批量查询）：**
```
orderIds = collect(orderIds)                  // 1次收集
orders = selectBatchIds(orderIds)             // 1次批量查询
memberIds = collect(memberIds)                // 1次收集
members = selectBatchIds(memberIds)           // 1次批量查询
levelIds = collect(levelIds)                  // 1次收集
levels = selectBatchIds(levelIds)             // 1次批量查询

for each table:
    order = orderMap.get(orderId)             // O(1) Map查找
    member = memberMap.get(memberId)           // O(1) Map查找
    level = levelMap.get(levelId)             // O(1) Map查找
```

### 优化效果

**TableController.getTableList():**
- 优化前：60次查询（20台台）
- 优化后：3次批量查询
- **性能提升：约 95%**

**OrderController.getActiveOrders():**
- 优化前：20次查询（10订单）
- 优化后：2次批量查询
- **性能提升：约 90%**

## 修改内容

### 1. TableServiceImpl.java

**新增方法：**

```java
/**
 * 批量预加载订单数据
 */
private Map<String, Order> preloadOrders(List<Table> tables) {
    List<String> orderIds = tables.stream()
            .map(Table::getCurrentOrderId)
            .filter(id -> id != null && !id.isEmpty())
            .distinct()
            .collect(Collectors.toList());

    if (orderIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<Order> orders = orderMapper.selectBatchIds(orderIds);
    return orders.stream().collect(Collectors.toMap(Order::getId, o -> o, (a, b) -> a));
}

/**
 * 批量预加载会员数据
 */
private Map<Long, Member> preloadMembers(Map<String, Order> orderMap) {
    List<Long> memberIds = orderMap.values().stream()
            .map(Order::getMemberId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

    if (memberIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<Member> members = memberMapper.selectBatchIds(memberIds);
    return members.stream().collect(Collectors.toMap(Member::getId, m -> m, (a, b) -> a));
}

/**
 * 批量预加载会员等级数据
 */
private Map<Long, MemberLevel> preloadMemberLevels(Map<Long, Member> memberMap) {
    List<Long> levelIds = memberMap.values().stream()
            .map(Member::getLevelId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

    if (levelIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<MemberLevel> levels = memberLevelMapper.selectBatchIds(levelIds);
    return levels.stream().collect(Collectors.toMap(MemberLevel::getId, l -> l, (a, b) -> a));
}
```

**修改 getTableList 方法：**
```java
public List<TableInfoResponse> getTableList(...) {
    // 查询桌台
    List<Table> tables = tableMapper.selectList(wrapper);

    // 批量预加载数据
    Map<String, Order> orderMap = preloadOrders(tables);
    Map<Long, Member> memberMap = preloadMembers(orderMap);
    Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);

    // 使用预加载数据转换
    List<TableInfoResponse> responses = tables.stream()
            .map(table -> convertToResponse(table, orderMap, memberMap, memberLevelMap))
            .collect(Collectors.toList());

    return responses;
}
```

**新增重载方法：**
```java
private TableInfoResponse convertToResponse(Table table,
                                               Map<String, Order> orderMap,
                                               Map<Long, Member> memberMap,
                                               Map<Long, MemberLevel> memberLevelMap) {
    // 使用 Map 查找替代数据库查询
    Order currentOrder = orderMap.get(table.getCurrentOrderId());
    Member member = memberMap.get(order.getMemberId());
    MemberLevel level = memberLevelMap.get(member.getLevelId());
    // ...
}
```

### 2. OrderServiceImpl.java

**新增 Mapper 注入：**
```java
@Resource
private MemberMapper memberMapper;

@Resource
private MemberLevelMapper memberLevelMapper;
```

**新增批量预加载方法：**
```java
private Map<Long, Member> preloadMembers(List<Order> orders) {
    List<Long> memberIds = orders.stream()
            .map(Order::getMemberId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

    if (memberIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<Member> members = memberMapper.selectBatchIds(memberIds);
    return members.stream().collect(Collectors.toMap(Member::getId, m -> m, (a, b) -> a));
}

private Map<Long, MemberLevel> preloadMemberLevels(Map<Long, Member> memberMap) {
    // 类似实现
}
```

**修改 getActiveOrders 方法：**
```java
public PageResult<OrderInfoResponse> getActiveOrders(Integer page, Integer pageSize) {
    Page<Order> resultPage = orderMapper.selectPage(pageObj, wrapper);

    // 批量预加载数据
    List<Order> orders = resultPage.getRecords();
    Map<Long, Member> memberMap = preloadMembers(orders);
    Map<Long, MemberLevel> memberLevelMap = preloadMemberLevels(memberMap);

    // 使用预加载数据转换
    List<OrderInfoResponse> records = orders.stream()
            .map(order -> convertToInfoResponse(order, memberMap, memberLevelMap))
            .collect(Collectors.toList());

    return pageResult;
}
```

## 其他改进

### 添加性能监控日志
```java
long startTime = System.currentTimeMillis();
// ... 业务逻辑
long endTime = System.currentTimeMillis();
log.info("获取桌台列表成功: count={},耗时={}ms", responses.size(), endTime - startTime);
```

### 兼容性处理
- 保留原有方法用于其他调用场景
- 新增重载方法用于批量查询优化
- 确保不影响现有功能

## 注意事项

1. **空值处理**：批量查询前需要检查集合是否为空
2. **Map 合并**：使用 `(a, b) -> a` 处理可能的重复键
3. **兼容性**：保留原有方法供其他地方调用
4. **日志优化**：减少不必要的日志输出

## 预期效果

| 接口 | 优化前查询次数 | 优化后查询次数 | 性能提升 |
|------|--------------|--------------|---------|
| getTableList (20台台) | 60次 | 3次 | ~95% |
| getActiveOrders (10订单) | 20次 | 2次 | ~90% |

@author wuci
