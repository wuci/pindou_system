# 会员管理功能设计文档

**日期**: 2026-03-31
**版本**: 1.0
**状态**: 设计阶段

## 一、功能概述

为桌台计时管理系统增加会员管理功能，支持会员信息管理、会员等级配置、会员折扣应用等功能。

### 核心需求

1. **会员信息录入**
   - 字段：名称、手机号、地址（非必填）
   - 新增会员默认为最低等级
   - 手机号唯一性校验

2. **会员等级配置（可配置）**
   - 默认等级：
     * 豆豆萌新（0～300元）- 9.5折
     * 熨烫能手（301～1000元）- 9折
     * 像素匠人（1001～3000元）- 8.5折
     * 熔豆典藏（3001元以上）- 8折
   - 支持自定义等级和折扣率

3. **会员折扣应用**
   - 结账时选择会员
   - 根据会员累计消费自动确定等级
   - 应用对应折扣计算实付金额

4. **累计消费更新**
   - 只有"已完成"状态的订单计入累计消费
   - "已作废"订单不计入

## 二、数据库设计

### 2.1 会员等级配置表 `biz_member_level`

```sql
CREATE TABLE `biz_member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最小累计金额',
  `max_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最大累计金额（null表示无上限）',
  `discount_rate` DECIMAL(4,3) NOT NULL COMMENT '折扣率（0.9表示9折）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_amount_range` (`min_amount`, `max_amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级配置表';

-- 插入默认等级数据
INSERT INTO `biz_member_level` (`name`, `min_amount`, `max_amount`, `discount_rate`, `sort`, `created_at`, `updated_at`) VALUES
('豆豆萌新', 0, 300, 0.950, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('熨烫能手', 300.01, 1000, 0.900, 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('像素匠人', 1000.01, 3000, 0.850, 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('熔豆典藏', 3000.01, NULL, 0.800, 4, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
```

### 2.2 会员表 `biz_member`

```sql
CREATE TABLE `biz_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `name` VARCHAR(50) NOT NULL COMMENT '会员名称',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计消费金额',
  `level_id` BIGINT NOT NULL COMMENT '会员等级ID',
  `created_at` BIGINT NOT NULL COMMENT '创建时间',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_total_amount` (`total_amount`),
  CONSTRAINT `fk_member_level` FOREIGN KEY (`level_id`) REFERENCES `biz_member_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';
```

### 2.3 订单表增加会员字段

```sql
ALTER TABLE `biz_order`
ADD COLUMN `member_id` BIGINT DEFAULT NULL COMMENT '会员ID' AFTER `operator_name`,
ADD COLUMN `original_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '原价（折扣前）' AFTER `amount`,
ADD INDEX `idx_member_id` (`member_id`);
```

## 三、后端设计

### 3.1 实体类 (Entity)

**Member.java**
```java
@Data
@TableName("biz_member")
public class Member {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String address;
    private BigDecimal totalAmount;
    private Long levelId;
    private Long createdAt;
    private Long updatedAt;
}
```

**MemberLevel.java**
```java
@Data
@TableName("biz_member_level")
public class MemberLevel {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal discountRate;
    private Integer sort;
    private Long createdAt;
    private Long updatedAt;
}
```

### 3.2 DTO 设计

**CreateMemberRequest.java**
```java
@Data
public class CreateMemberRequest {
    @NotBlank(message = "会员名称不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String address;
}
```

**UpdateMemberRequest.java**
```java
@Data
public class UpdateMemberRequest {
    private Long id;
    private String name;
    private String address;
}
```

**MemberResponse.java**
```java
@Data
public class MemberResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private BigDecimal totalAmount;
    private Long levelId;
    private String levelName;
    private BigDecimal discountRate;
    private Long createdAt;
    private Long updatedAt;
}
```

**CreateMemberLevelRequest.java**
```java
@Data
public class CreateMemberLevelRequest {
    @NotBlank(message = "等级名称不能为空")
    private String name;

    @NotNull(message = "最小金额不能为空")
    @DecimalMin(value = "0", message = "最小金额不能小于0")
    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    @NotNull(message = "折扣率不能为空")
    @DecimalMin(value = "0.1", message = "折扣率不能小于0.1")
    @DecimalMax(value = "1.0", message = "折扣率不能大于1.0")
    private BigDecimal discountRate;

    private Integer sort;
}
```

### 3.3 Controller 接口

**MemberController.java**
```java
@RestController
@RequestMapping("/members")
public class MemberController {

    // POST /members - 创建会员
    // GET /members - 会员列表（分页、搜索）
    // GET /members/{id} - 会员详情
    // PUT /members/{id} - 更新会员信息
    // DELETE /members/{id} - 删除会员
    // GET /members/search - 根据手机号搜索会员
}
```

**MemberLevelController.java**
```java
@RestController
@RequestMapping("/member-levels")
public class MemberLevelController {

    // GET /member-levels - 获取所有等级
    // POST /member-levels - 创建等级
    // PUT /member-levels/{id} - 更新等级
    // DELETE /member-levels/{id} - 删除等级
    // POST /member-levels/init - 初始化默认等级
}
```

### 3.4 Service 层核心逻辑

**MemberService**
```java
public interface MemberService {
    // 创建会员（自动分配最低等级）
    MemberResponse createMember(CreateMemberRequest request);

    // 根据累计消费计算会员等级
    Long calculateLevelId(BigDecimal totalAmount);

    // 更新会员累计消费并重新计算等级
    void updateTotalAmount(Long memberId, BigDecimal orderAmount);

    // 根据会员ID和订单金额计算折扣
    DiscountDetail calculateDiscount(Long memberId, BigDecimal originalAmount);
}
```

**MemberLevelService**
```java
public interface MemberLevelService {
    // 获取所有等级（按金额范围排序）
    List<MemberLevel> getAllLevels();

    // 初始化默认等级
    void initDefaultLevels();

    // 根据累计金额查找对应等级
    MemberLevel findLevelByAmount(BigDecimal amount);
}
```

### 3.5 核心流程

#### 创建会员流程
```
1. 验证手机号唯一性
2. 创建会员记录：
   - total_amount = 0
   - level_id = 最低等级ID
3. 返回会员信息（包含等级名称和折扣）
```

#### 会员等级自动升级流程
```
1. 订单完成后触发（status=completed）
2. member.total_amount += order.amount
3. 根据新的total_amount查询对应等级
4. 如果level_id变化，更新会员等级
5. 记录等级变更日志（可选）
```

#### 折扣计算流程
```
1. 获取会员信息
2. 获取会员对应的等级折扣率
3. 折扣金额 = 原价 × (1 - 折扣率)
4. 实付金额 = 原价 - 折扣金额
```

## 四、前端设计

### 4.1 新增页面

#### 1. 会员管理页面 `/members`

**路由配置**
```typescript
{
  path: '/members',
  name: 'MemberManagement',
  component: () => import('@/views/MemberManagement/index.vue'),
  meta: { title: '会员管理', icon: 'User' }
}
```

**页面功能**
- 会员列表（表格展示）
  - 列字段：ID、名称、手机号、地址、累计消费、等级、折扣率、注册时间
  - 搜索：手机号/会员名称
  - 分页
- 新增会员按钮 → 弹出对话框
- 编辑会员按钮 → 弹出对话框
- 删除会员按钮 → 二次确认

**新增/编辑会员对话框**
- 表单字段：
  - 会员名称（必填）
  - 手机号（必填，新增时唯一性校验）
  - 地址（选填）

#### 2. 会员等级配置 `/settings/member-levels`

**在设置页面下新增会员等级配置卡片**

**配置内容**
- 等级列表（表格展示）
  - 列字段：等级名称、金额范围、折扣率、操作
- 新增等级按钮
- 编辑等级按钮
- 删除等级按钮
- 初始化默认等级按钮

**新增/编辑等级对话框**
- 表单字段：
  - 等级名称（必填）
  - 最小金额（必填，数字）
  - 最大金额（选填，数字，可空表示无上限）
  - 折扣率（必填，0.1-1.0）
  - 排序（必填，数字）

### 4.2 结账对话框改造

**BillDialog.vue 新增内容**

```vue
<template>
  <el-dialog title="结账确认">
    <!-- 原有内容：桌台信息、时间信息、费用明细 -->

    <!-- 新增：会员选择 -->
    <el-form-item label="会员">
      <el-select
        v-model="selectedMemberId"
        filterable
        remote
        reserve-keyword
        placeholder="输入手机号搜索会员"
        :remote-method="searchMembers"
        :loading="searching"
        clearable
        @change="handleMemberChange"
      >
        <el-option
          v-for="member in memberOptions"
          :key="member.id"
          :label="`${member.name} (${member.phone})`"
          :value="member.id"
        >
          <span>{{ member.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 12px">
            {{ member.phone }}
          </span>
        </el-option>
      </el-select>
      <el-button
        v-if="selectedMemberId"
        type="primary"
        link
        @click="showMemberDetail = true"
      >
        查看详情
      </el-button>
    </el-form-item>

    <!-- 新增：会员折扣信息 -->
    <div v-if="selectedMember" class="member-discount">
      <div class="discount-row">
        <span class="label">会员等级：</span>
        <span class="value">{{ selectedMember.levelName }}</span>
      </div>
      <div class="discount-row">
        <span class="label">折扣率：</span>
        <span class="value highlight">{{ (selectedMember.discountRate * 10).toFixed(1) }}折</span>
      </div>
      <div class="discount-row">
        <span class="label">原价：</span>
        <span class="value">¥{{ formatMoney(originalAmount) }}</span>
      </div>
      <div class="discount-row">
        <span class="label">折扣优惠：</span>
        <span class="value discount">-¥{{ formatMoney(discountAmount) }}</span>
      </div>
    </div>

    <!-- 修改后的费用明细 -->
    <div class="bill-section">
      <h3 class="section-title">费用明细</h3>
      <div class="amount-row">
        <span class="label">正常费用</span>
        <span class="value">¥{{ formatMoney(normalAmount) }}</span>
      </div>
      <div class="amount-row total">
        <span class="label">总计（折后）</span>
        <span class="value total-amount">¥{{ formatMoney(finalAmount) }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
const selectedMemberId = ref<number | null>(null)
const selectedMember = ref<Member | null>(null)
const memberOptions = ref<Member[]>([])
const searching = ref(false)

// 搜索会员
const searchMembers = async (query: string) => {
  if (!query) {
    memberOptions.value = []
    return
  }
  searching.value = true
  try {
    const members = await searchMembersAPI(query)
    memberOptions.value = members
  } finally {
    searching.value = false
  }
}

// 会员选择变更
const handleMemberChange = (memberId: number | null) => {
  if (memberId) {
    selectedMember.value = memberOptions.value.find(m => m.id === memberId)
    recalculateAmount()
  } else {
    selectedMember.value = null
    recalculateAmount()
  }
}

// 重新计算金额
const recalculateAmount = () => {
  if (selectedMember.value) {
    discountAmount.value = originalAmount.value * (1 - selectedMember.value.discountRate)
    finalAmount.value = originalAmount.value - discountAmount.value
  } else {
    discountAmount.value = 0
    finalAmount.value = originalAmount.value
  }
}

// 确认结账时传递会员ID
const handleConfirm = async () => {
  await endTable(props.tableId, {
    memberId: selectedMemberId.value
  })
}
</script>
```

### 4.3 订单详情显示

**订单列表和详情新增字段**
- 会员姓名
- 会员等级
- 原价
- 折扣金额
- 实付金额

## 五、技术实现要点

### 5.1 会员等级查询逻辑

```java
// 根据累计消费金额查询对应等级
public MemberLevel findLevelByAmount(BigDecimal amount) {
    LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
    wrapper.le(MemberLevel::getMinAmount, amount)
           .and(w -> w.isNull(MemberLevel::getMaxAmount)
                      .or()
                      .ge(MemberLevel::getMaxAmount, amount))
           .orderByDesc(MemberLevel::getSort)
           .last("LIMIT 1");
    return memberLevelMapper.selectOne(wrapper);
}
```

### 5.2 折扣计算精度

```java
// 使用 BigDecimal 确保金额计算精度
public DiscountDetail calculateDiscount(Long memberId, BigDecimal originalAmount) {
    Member member = memberMapper.selectById(memberId);
    MemberLevel level = memberLevelMapper.selectById(member.getLevelId());

    BigDecimal discountRate = level.getDiscountRate();
    BigDecimal discountAmount = originalAmount.multiply(
        BigDecimal.ONE.subtract(discountRate)
    ).setScale(2, RoundingMode.HALF_UP);

    BigDecimal finalAmount = originalAmount.subtract(discountAmount);

    return new DiscountDetail(
        originalAmount,
        discountRate,
        discountAmount,
        finalAmount
    );
}
```

### 5.3 订单完成后更新会员累计消费

```java
@Transactional
public void updateTotalAmount(Long memberId, BigDecimal orderAmount) {
    // 1. 更新累计消费
    Member member = memberMapper.selectById(memberId);
    BigDecimal oldTotal = member.getTotalAmount();
    BigDecimal newTotal = oldTotal.add(orderAmount);
    member.setTotalAmount(newTotal);

    // 2. 重新计算等级
    Long newLevelId = calculateLevelId(newTotal);

    if (!newLevelId.equals(member.getLevelId())) {
        member.setLevelId(newLevelId);
        // 记录等级变更日志
        logLevelUpgrade(member.getId(), member.getLevelId(), newLevelId, oldTotal, newTotal);
    }

    member.setUpdatedAt(System.currentTimeMillis());
    memberMapper.updateById(member);
}
```

### 5.4 手机号唯一性校验

```java
public void validatePhoneUnique(String phone, Long excludeId) {
    LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Member::getPhone, phone);
    if (excludeId != null) {
        wrapper.ne(Member::getId, excludeId);
    }
    Long count = memberMapper.selectCount(wrapper);
    if (count > 0) {
        throw new BusinessException(ErrorCode.MEMBER_PHONE_EXISTS);
    }
}
```

## 六、实施计划

### 阶段一：数据库和基础接口
1. 创建数据库表和初始化脚本
2. 创建实体类、DTO
3. 实现会员等级配置CRUD
4. 实现会员CRUD基础接口

### 阶段二：会员折扣功能
1. 实现会员等级计算逻辑
2. 实现折扣计算逻辑
3. 改造结账对话框，集成会员选择和折扣显示
4. 修改订单保存逻辑，支持会员关联

### 阶段三：累计消费和等级升级
1. 订单完成后触发累计消费更新
2. 实现会员等级自动升级
3. 订单列表显示会员相关信息

### 阶段四：前端页面
1. 会员管理页面
2. 会员等级配置页面
3. 结账对话框改造
4. 订单详情显示会员信息

## 七、测试要点

1. **会员创建**
   - 手机号唯一性校验
   - 默认等级分配正确
   - 必填/非必填字段验证

2. **会员等级**
   - 金额范围边界值测试
   - 折扣率范围验证（0.1-1.0）
   - 等级排序正确

3. **折扣计算**
   - 折扣金额计算精度（保留2位小数）
   - 边界值测试（0元、极大金额）
   - 不同等级折扣正确性

4. **累计消费和升级**
   - 已完成订单正确累加
   - 已作废订单不累加
   - 跨等级升级正确

5. **并发测试**
   - 同一会员多笔订单同时完成
   - 等级升级的并发安全

## 八、后续扩展

1. **会员权益扩展**
   - 积分系统
   - 优惠券
   - 生日特权

2. **会员统计**
   - 消费趋势分析
   - 等级分布统计
   - 活跃度分析

3. **会员营销**
   - 等级保级规则
   - 升级加速活动
   - 推荐奖励
