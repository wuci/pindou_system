# 会员管理功能 - 数据模型

## 实体定义

### Member (会员)

| 字段 | 类型 | 说明 | 验证规则 | 默认值 |
|------|------|------|----------|--------|
| id | BIGINT | 主键 | 自增 | AUTO |
| name | VARCHAR(50) | 会员名称 | @NotBlank, 1-50字符 | - |
| phone | VARCHAR(20) | 手机号 | @NotBlank, @Pattern, 唯一 | - |
| address | VARCHAR(200) | 地址 | 可选, 0-200字符 | NULL |
| totalAmount | DECIMAL(10,2) | 累计消费金额 | ≥ 0 | 0 |
| levelId | BIGINT | 会员等级ID | @NotNull, FK → member_level | - |
| createdAt | BIGINT | 创建时间 | 毫秒时间戳 | - |
| updatedAt | BIGINT | 更新时间 | 毫秒时间戳 | - |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY uk_phone (phone)
- INDEX idx_level_id (levelId)
- INDEX idx_total_amount (totalAmount)

### MemberLevel (会员等级)

| 字段 | 类型 | 说明 | 验证规则 | 默认值 |
|------|------|------|----------|--------|
| id | BIGINT | 主键 | 自增 | AUTO |
| name | VARCHAR(50) | 等级名称 | @NotBlank, 1-50字符 | - |
| minAmount | DECIMAL(10,2) | 最小累计金额 | @NotNull, ≥ 0 | - |
| maxAmount | DECIMAL(10,2) | 最大累计金额 | 可选, > minAmount 或 NULL | NULL |
| discountRate | DECIMAL(4,3) | 折扣率 | @NotNull, 0.1-1.0 | - |
| sort | INT | 排序 | - | 0 |
| createdAt | BIGINT | 创建时间 | 毫秒时间戳 | - |
| updatedAt | BIGINT | 更新时间 | 毫秒时间戳 | - |

**索引**:
- PRIMARY KEY (id)
- INDEX idx_amount_range (min_amount, max_amount)

### Order (订单) - 新增字段

| 字段 | 类型 | 说明 | 验证规则 | 默认值 |
|------|------|------|----------|--------|
| memberId | BIGINT | 会员ID | 可选, FK → member | NULL |
| originalAmount | DECIMAL(10,2) | 原价（折扣前） | ≥ 0 | 0 |

**索引**:
- INDEX idx_member_id (member_id)

## 关系图

```
MemberLevel (1) <-----> (N) Member
                      (1)
                       (1)
                       (0/N)
                      Order
```

## 状态转换

### 会员等级升级流程

```
累计消费增加
    ↓
查询新等级 (findLevelByAmount)
    ↓
等级是否变化?
    ├─ 是 → 更新level_id
    └─ 否 → 保持不变
```

### 订单状态与累计消费

```
订单完成 (endTimer)
    ↓
订单状态 = completed?
    ├─ 是 → 更新会员累计消费 → 可能触发等级升级
    └─ 否 (cancelled) → 不更新累计消费
```

## 默认数据

### 会员等级初始数据

| id | name | minAmount | maxAmount | discountRate | sort |
|----|------|-----------|-----------|--------------|------|
| 1 | 豆豆萌新 | 0.00 | 300.00 | 0.950 | 1 |
| 2 | 熨烫能手 | 300.01 | 1000.00 | 0.900 | 2 |
| 3 | 像素匠人 | 1000.01 | 3000.00 | 0.850 | 3 |
| 4 | 熔豆典藏 | 3000.01 | NULL | 0.800 | 4 |

## 查询示例

### 根据累计消费查询等级

```sql
SELECT * FROM biz_member_level
WHERE min_amount <= #{amount}
  AND (max_amount IS NULL OR max_amount >= #{amount})
ORDER BY sort DESC
LIMIT 1;
```

### 查询会员及其等级信息

```sql
SELECT m.*, ml.name as level_name, ml.discount_rate
FROM biz_member m
LEFT JOIN biz_member_level ml ON m.level_id = ml.id
WHERE m.phone = #{phone};
```
