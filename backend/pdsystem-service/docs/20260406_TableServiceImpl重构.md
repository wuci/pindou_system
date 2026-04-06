# TableServiceImpl 重构记录

**日期**: 2026-04-06
**作者**: wuci
**重构原因**: 代码存在架构问题，需要进行优化

## 补充更新

### 依赖注入方式调整
将构造器注入改为字段注入 + @Autowired 方式，符合团队编码规范。

**修改的文件**:
- `TableServiceImpl.java`
- `MemberDiscountServiceImpl.java`
- `OrderSettlementServiceImpl.java`
- `DiscountServiceImpl.java`

**修改前**:
```java
private final MemberService memberService;

public TableServiceImpl(MemberService memberService) {
    this.memberService = memberService;
}
```

**修改后**:
```java
@Autowired
private MemberService memberService;
```

## 问题分析

### 原代码问题
1. **类过大** - 1468行代码，违反单一职责原则
2. **魔法值** - "idle", "using", "paused" 等硬编码字符串散布在代码中
3. **职责混乱** - 混合了桌台管理、订单管理、会员管理、计费等多种职责
4. **重复代码** - 会员折扣计算逻辑重复
5. **内部类** - 静态内部类应提取为独立的值对象

## 重构方案

### 1. 创建常量类
**文件**: `TableConstants.java`
**位置**: `pdsystem-entity/src/main/java/com/pindou/timer/constants/`

- 封装所有桌台相关的常量
- 提供静态方法判断状态
- 消除魔法值

### 2. 提取值对象
**文件**: `DurationCalculation.java`
- 时长计算结果值对象
- 提供时长转换方法

**文件**: `OrderAmountCalculation.java`
- 订单金额计算结果值对象
- 提供折扣相关计算方法

### 3. 提取专门服务
**文件**: `MemberDiscountService.java` / `MemberDiscountServiceImpl.java`
- 专门处理会员折扣相关逻辑
- 提供折扣计算和判断方法

**文件**: `OrderSettlementService.java` / `OrderSettlementServiceImpl.java`
- 专门处理订单结算相关逻辑
- 包含时长计算、订单状态判断、费用计算等

### 4. 重构 TableServiceImpl
- 使用常量替代魔法值
- 使用值对象封装计算结果
- 委托专门服务处理复杂逻辑
- 方法拆分，提高可读性

## 重构效果

### 代码行数
- 原代码: 1468 行
- 重构后主类: 约 800 行
- 提取的服务类: 约 400 行

### 可维护性提升
1. 职责清晰 - 每个类只负责一个领域的业务
2. 常量统一 - 所有状态和配置都在常量类中
3. 可测试性提高 - 服务类可以独立测试
4. 扩展性增强 - 新增功能只需要修改对应的服务类

### 架构改进
```
TableServiceImpl (桌台管理)
    ├── TableConstants (常量)
    ├── MemberDiscountService (会员折扣)
    ├── OrderSettlementService (订单结算)
    ├── DurationCalculation (值对象)
    └── OrderAmountCalculation (值对象)
```

## 新增文件清单

1. `pdsystem-entity`:
   - `constants/TableConstants.java`
   - `dto/DurationCalculation.java`
   - `dto/OrderAmountCalculation.java`

2. `pdsystem-service`:
   - `service/MemberDiscountService.java`
   - `service/impl/MemberDiscountServiceImpl.java`
   - `service/OrderSettlementService.java`
   - `service/impl/OrderSettlementServiceImpl.java`

## 修改文件清单

1. `pdsystem-service/service/impl/TableServiceImpl.java` - 完全重构

## 注意事项

1. 所有新增类都遵循项目编码规范
2. 使用了依赖注入，提高可测试性
3. 保持了原有功能不变
4. 添加了详细的注释
