# 支付方式功能实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 为桌台计时系统添加支付方式功能，支持线下、线上、会员余额和组合支付

**架构:**
- 开始计时时选择并记录支付方式，不扣费
- 结账时根据支付方式扣除会员余额（如适用）
- 组合支付由系统自动计算余额使用量

**技术栈:** Java Spring Boot, Vue 3, MySQL, Element Plus

---

## Chunk 1: 数据库迁移

### Task 1: 创建数据库迁移脚本

**Files:**
- Create: `backend/sql/2026_04_02_add_payment_method.sql`

- [ ] **Step 1: 编写数据库迁移脚本**

```sql
-- 为订单表添加支付方式相关字段
USE `pindou_timer`;

ALTER TABLE `biz_order`
ADD COLUMN `payment_method` VARCHAR(20) DEFAULT 'offline' COMMENT '支付方式:offline-线下,online-线上,balance-余额,combined-组合' AFTER `status`,
ADD COLUMN `balance_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '余额支付金额' AFTER `payment_method`,
ADD COLUMN `other_payment_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '其他方式支付金额' AFTER `balance_amount`;

-- 添加索引
ALTER TABLE `biz_order`
ADD KEY `idx_payment_method` (`payment_method`);

-- 查看表结构确认
DESCRIBE `biz_order`;
```

- [ ] **Step 2: 提交迁移脚本**

```bash
cd D:/ai_project/pindouTimer
git add backend/sql/2026_04_02_add_payment_method.sql
git commit -m "feat: 添加支付方式字段迁移脚本"
```

### Task 2: 执行数据库迁移

**Files:**
- N/A (数据库操作)

- [ ] **Step 1: 执行迁移脚本**

使用您的数据库客户端（Navicat、phpMyAdmin等）连接到数据库并执行：
```bash
mysql -u root -p123456 pindou_timer < backend/sql/2026_04_02_add_payment_method.sql
```

- [ ] **Step 2: 验证字段已添加**

查询验证：
```sql
SHOW COLUMNS FROM biz_order LIKE 'payment%';
```

预期输出：应看到 `payment_method`, `balance_amount`, `other_payment_amount` 三个字段

---

## Chunk 2: 后端实体和DTO修改

### Task 3: 修改Order实体类

**Files:**
- Modify: `backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/Order.java`

- [ ] **Step 1: 添加支付方式字段到Order实体**

在 `Order.java` 的 `paidAt` 字段后添加：

```java
@Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合")
@TableField("payment_method")
private String paymentMethod;

@Schema(description = "余额支付金额")
@TableField("balance_amount")
private BigDecimal balanceAmount;

@Schema(description = "其他方式支付金额")
@TableField("other_payment_amount")
private BigDecimal otherPaymentAmount;
```

- [ ] **Step 2: 提交修改**

```bash
cd D:/ai_project/pindouTimer
git add backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/Order.java
git commit -m "feat: Order实体添加支付方式字段"
```

### Task 4: 修改StartTimerRequest DTO

**Files:**
- Modify: `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/StartTimerRequest.java`

- [ ] **Step 1: 添加支付方式字段**

在 `StartTimerRequest.java` 的 `note` 字段后添加：

```java
@Schema(description = "支付方式：offline-线下, online-线上, balance-余额, combined-组合", example = "offline")
private String paymentMethod;
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/StartTimerRequest.java
git commit -m "feat: StartTimerRequest添加支付方式字段"
```

### Task 5: 修改TableInfoResponse DTO

**Files:**
- Modify: `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/TableInfoResponse.java`

- [ ] **Step 1: 添加支付方式字段**

在 `TableInfoResponse.java` 的 `endTime` 字段后添加：

```java
@Schema(description = "支付方式")
private String paymentMethod;

@Schema(description = "余额支付金额")
private Double balanceAmount;

@Schema(description = "其他方式支付金额")
private Double otherPaymentAmount;
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/TableInfoResponse.java
git commit -m "feat: TableInfoResponse添加支付方式字段"
```

### Task 6: 修改BillResponse DTO

**Files:**
- Modify: `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/BillResponse.java`

- [ ] **Step 1: 添加支付方式明细字段**

查看 `BillResponse.java` 并在适当位置添加：

```java
@Schema(description = "支付方式")
private String paymentMethod;

@Schema(description = "余额支付金额")
private Double balanceAmount;

@Schema(description = "其他方式支付金额")
private Double otherPaymentAmount;
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/BillResponse.java
git commit -m "feat: BillResponse添加支付方式明细字段"
```

---

## Chunk 3: 后端Service层修改

### Task 7: 修改TableServiceImpl - 开始计时逻辑

**Files:**
- Modify: `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`

- [ ] **Step 1: 修改startTimer方法保存支付方式**

在 `TableServiceImpl.java` 的 `startTimer` 方法中，找到创建订单的部分，添加支付方式设置：

```java
// 在订单创建部分添加
order.setPaymentMethod(request.getPaymentMethod());
order.setBalanceAmount(BigDecimal.ZERO);
order.setOtherPaymentAmount(BigDecimal.ZERO);

// 日志记录
log.info("开始计时 - 桌台ID:{}, 支付方式:{}, 会员ID:{}", tableId, request.getPaymentMethod(), request.getMemberId());
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java
git commit -m "feat: 开始计时保存支付方式"
```

### Task 8: 修改TableServiceImpl - 结账余额扣除逻辑

**Files:**
- Modify: `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`

- [ ] **Step 1: 在endTimer方法中添加余额扣除逻辑**

在 `TableServiceImpl.java` 的 `endTimer` 方法中，添加余额处理：

```java
// 在计算费用后、更新订单前添加
// 处理余额扣除
if (order.getMemberId() != null && order.getPaymentMethod() != null) {
    String paymentMethod = order.getPaymentMethod();
    BigDecimal finalAmount = order.getAmount();

    if ("balance".equals(paymentMethod) || "combined".equals(paymentMethod)) {
        // 查询会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员信息不存在");
        }

        BigDecimal balance = member.getBalance();
        BigDecimal balanceAmount = BigDecimal.ZERO;
        BigDecimal otherAmount = BigDecimal.ZERO;

        if ("balance".equals(paymentMethod)) {
            // 纯余额支付
            if (balance.compareTo(finalAmount) < 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "余额不足，当前余额：" + balance + "元，需支付：" + finalAmount + "元");
            }
            balanceAmount = finalAmount;
        } else {
            // 组合支付：优先使用余额
            if (balance.compareTo(finalAmount) >= 0) {
                // 余额充足，全部用余额
                balanceAmount = finalAmount;
            } else {
                // 余额不足，用完余额，剩余线下支付
                balanceAmount = balance;
                otherAmount = finalAmount.subtract(balance);
            }
        }

        // 扣除余额
        member.setBalance(member.getBalance().subtract(balanceAmount));
        member.setTotalAmount(member.getTotalAmount().add(finalAmount));
        memberMapper.updateById(member);

        // 更新订单支付信息
        order.setBalanceAmount(balanceAmount);
        order.setOtherPaymentAmount(otherAmount);

        log.info("余额扣除成功 - 会员ID:{}, 扣除金额:{}, 余额剩余:{}",
            member.getId(), balanceAmount, member.getBalance());
    }
}
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java
git commit -m "feat: 结账时处理余额扣除逻辑"
```

### Task 9: 修改convertToResponse方法

**Files:**
- Modify: `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`

- [ ] **Step 1: 在convertToResponse方法中添加支付方式字段映射**

找到 `convertToResponse` 方法，在返回前添加：

```java
// 设置支付方式信息
response.setPaymentMethod(table.getCurrentOrder() != null ? table.getCurrentOrder().getPaymentMethod() : null);
if (table.getCurrentOrder() != null) {
    response.setBalanceAmount(table.getCurrentOrder().getBalanceAmount() != null ?
        table.getCurrentOrder().getBalanceAmount().doubleValue() : null);
    response.setOtherPaymentAmount(table.getCurrentOrder().getOtherPaymentAmount() != null ?
        table.getCurrentOrder().getOtherPaymentAmount().doubleValue() : null);
}
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java
git commit -m "feat: convertToResponse添加支付方式信息"
```

### Task 10: 修改getBill方法返回支付信息

**Files:**
- Modify: `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`

- [ ] **Step 1: 在getBill方法中设置支付方式信息**

找到 `getBill` 方法，在构建 `BillResponse` 时添加：

```java
// 设置支付方式信息
if (order != null) {
    response.setPaymentMethod(order.getPaymentMethod());
    response.setBalanceAmount(order.getBalanceAmount() != null ?
        order.getBalanceAmount().doubleValue() : null);
    response.setOtherPaymentAmount(order.getOtherPaymentAmount() != null ?
        order.getOtherPaymentAmount().doubleValue() : null);
}
```

- [ ] **Step 2: 提交修改**

```bash
git add backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java
git commit -m "feat: getBill返回支付方式信息"
```

---

## Chunk 4: 前端类型定义

### Task 11: 添加支付方式类型定义

**Files:**
- Modify: `frontend/src/api/table.ts`

- [ ] **Step 1: 添加支付方式枚举和接口**

在 `table.ts` 中添加：

```typescript
/**
 * 支付方式枚举
 */
export type PaymentMethod = 'offline' | 'online' | 'balance' | 'combined'

/**
 * 支付方式选项
 */
export interface PaymentMethodOption {
  value: PaymentMethod
  label: string
  description?: string
}

/**
 * 非会员支付方式
 */
export const NON_MEMBER_PAYMENT_METHODS: PaymentMethodOption[] = [
  { value: 'offline', label: '线下支付', description: '店内现场支付' },
  { value: 'online', label: '线上支付', description: '平台订单支付' }
]

/**
 * 会员支付方式
 */
export const MEMBER_PAYMENT_METHODS: PaymentMethodOption[] = [
  { value: 'offline', label: '线下支付', description: '店内现场支付' },
  { value: 'online', label: '线上支付', description: '平台订单支付' },
  { value: 'balance', label: '会员余额', description: '使用会员余额支付' },
  { value: 'combined', label: '组合支付', description: '余额+线下组合支付' }
]

/**
 * 支付方式显示文本映射
 */
export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  offline: '线下支付',
  online: '线上支付',
  balance: '会员余额',
  combined: '组合支付'
}
```

- [ ] **Step 2: 修改StartTableParams接口**

在 `table.ts` 中找到 `StartTableParams` 接口，添加：

```typescript
export interface StartTableParams {
  presetDuration: number | null
  channel?: string
  memberId?: number
  remark?: string
  paymentMethod?: PaymentMethod
}
```

- [ ] **Step 3: 修改TableInfo接口**

在 `table.ts` 的 `TableInfo` 接口中添加：

```typescript
export interface TableInfo {
  // ... 现有字段
  paymentMethod?: PaymentMethod
  balanceAmount?: number
  otherPaymentAmount?: number
}
```

- [ ] **Step 4: 提交修改**

```bash
cd D:/ai_project/pindouTimer
git add frontend/src/api/table.ts
git commit -m "feat: 添加支付方式类型定义"
```

---

## Chunk 5: 前端开始计时对话框

### Task 12: 修改StartTimerDialog添加支付方式选择

**Files:**
- Modify: `frontend/src/components/StartTimerDialog.vue`

- [ ] **Step 1: 添加支付方式相关响应式数据**

在 `<script setup>` 中添加：

```typescript
import { NON_MEMBER_PAYMENT_METHODS, MEMBER_PAYMENT_METHODS, type PaymentMethod } from '@/api/table'

// 支付方式
const selectedPaymentMethod = ref<PaymentMethod>('offline')

// 可用的支付方式列表
const availablePaymentMethods = computed(() => {
  return selectedMember.value ? MEMBER_PAYMENT_METHODS : NON_MEMBER_PAYMENT_METHODS
})

// 组合支付信息计算
const combinedPaymentInfo = computed(() => {
  if (selectedPaymentMethod.value !== 'combined' || !selectedMember.value) {
    return null
  }

  const balance = selectedMember.value.balance || 0
  const totalAmount = discountedPrice.value || originalPrice.value || 0

  if (balance >= totalAmount) {
    return {
      balanceAmount: totalAmount,
      otherAmount: 0,
      description: '余额充足，全部使用余额支付'
    }
  } else {
    return {
      balanceAmount: balance,
      otherAmount: totalAmount - balance,
      description: `余额${balance.toFixed(2)}元 + 线下${(totalAmount - balance).toFixed(2)}元`
    }
  }
})
```

- [ ] **Step 2: 在会员切换时重置支付方式**

修改 `handleMemberToggle` 函数：

```typescript
const handleMemberToggle = (value: boolean) => {
  if (!value) {
    selectedMember.value = null
  }
  // 切换会员状态时，重置支付方式为默认值
  selectedPaymentMethod.value = 'offline'
}
```

- [ ] **Step 3: 添加支付方式选择UI**

在"备注"字段之前添加支付方式选择：

```vue
<!-- 支付方式 -->
<el-form-item label="支付方式" required>
  <el-radio-group v-model="selectedPaymentMethod" class="payment-method-group">
    <el-radio
      v-for="method in availablePaymentMethods"
      :key="method.value"
      :value="method.value"
      class="payment-method-radio"
    >
      <div class="payment-method-content">
        <span class="payment-method-label">{{ method.label }}</span>
        <span v-if="method.description" class="payment-method-desc">{{ method.description }}</span>
        <span v-if="method.value === 'balance' && selectedMember" class="payment-method-balance">
          余额: ¥{{ selectedMember.balance.toFixed(2) }}
        </span>
      </div>
    </el-radio>
  </el-radio-group>
</el-form-item>

<!-- 组合支付明细 -->
<el-form-item v-if="selectedPaymentMethod === 'combined' && combinedPaymentInfo" label="支付明细">
  <div class="combined-payment-info">
    <div class="payment-info-item">
      <span class="payment-info-label">余额支付:</span>
      <span class="payment-info-value balance">¥{{ combinedPaymentInfo.balanceAmount.toFixed(2) }}</span>
    </div>
    <div v-if="combinedPaymentInfo.otherAmount > 0" class="payment-info-item">
      <span class="payment-info-label">线下支付:</span>
      <span class="payment-info-value other">¥{{ combinedPaymentInfo.otherAmount.toFixed(2) }}</span>
    </div>
    <div class="payment-info-total">
      合计: ¥{{ (combinedPaymentInfo.balanceAmount + combinedPaymentInfo.otherAmount).toFixed(2) }}
    </div>
  </div>
</el-form-item>
```

- [ ] **Step 4: 修改handleConfirm传递支付方式**

修改 `handleConfirm` 函数中的 `startTable` 调用：

```typescript
await startTable(props.table.id, {
  presetDuration: packageDuration,
  channel: selectedChannel.value,
  memberId: selectedMember.value?.id,
  remark: form.value.remark,
  paymentMethod: selectedPaymentMethod.value
})
```

- [ ] **Step 5: 在initializeForm中初始化支付方式**

在 `initializeForm` 函数中添加：

```typescript
// 重置支付方式为默认值
selectedPaymentMethod.value = 'offline'
```

- [ ] **Step 6: 添加样式**

在 `<style scoped>` 中添加：

```scss
.payment-method-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.payment-method-radio {
  margin-right: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px 16px;
  transition: all 0.3s;
}

.payment-method-radio:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.payment-method-radio.is-checked {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.payment-method-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.payment-method-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.payment-method-desc {
  font-size: 12px;
  color: #909399;
}

.payment-method-balance {
  font-size: 13px;
  color: #67c23a;
  font-weight: 500;
}

.combined-payment-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #67c23a;
  border-radius: 4px;
}

.payment-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-info-label {
  font-size: 14px;
  color: #606266;
}

.payment-info-value {
  font-size: 16px;
  font-weight: 600;

  &.balance {
    color: #67c23a;
  }

  &.other {
    color: #409eff;
  }
}

.payment-info-total {
  padding-top: 8px;
  border-top: 1px dashed #67c23a;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  text-align: right;
}

:deep(.el-radio) {
  margin-right: 0;
  width: 100%;
}

:deep(.el-radio__label) {
  width: 100%;
}
```

- [ ] **Step 7: 提交修改**

```bash
git add frontend/src/components/StartTimerDialog.vue
git commit -m "feat: 开始计时对话框添加支付方式选择"
```

---

## Chunk 6: 前端结账对话框

### Task 13: 修改BillDialog显示支付明细

**Files:**
- Modify: `frontend/src/components/BillDialog.vue`

- [ ] **Step 1: 添加支付信息显示**

在账单明细区域添加支付方式信息：

```vue
<!-- 支付方式信息 -->
<el-divider v-if="billDetail.paymentMethod" content-position="left">支付信息</el-divider>

<div v-if="billDetail.paymentMethod" class="payment-info-section">
  <div class="payment-method-display">
    <span class="payment-label">支付方式:</span>
    <el-tag :type="getPaymentMethodTagType(billDetail.paymentMethod)">
      {{ getPaymentMethodLabel(billDetail.paymentMethod) }}
    </el-tag>
  </div>

  <!-- 组合支付明细 -->
  <template v-if="billDetail.paymentMethod === 'combined'">
    <div class="payment-detail-item">
      <span class="payment-detail-label">余额支付:</span>
      <span class="payment-detail-value balance">
        ¥{{ (billDetail.balanceAmount || 0).toFixed(2) }}
      </span>
    </div>
    <div v-if="billDetail.otherPaymentAmount > 0" class="payment-detail-item">
      <span class="payment-detail-label">线下支付:</span>
      <span class="payment-detail-value other">
        ¥{{ (billDetail.otherPaymentAmount || 0).toFixed(2) }}
      </span>
    </div>
  </template>

  <!-- 纯余额支付 -->
  <template v-else-if="billDetail.paymentMethod === 'balance' && billDetail.balanceAmount > 0">
    <div class="payment-detail-item">
      <span class="payment-detail-label">余额支付:</span>
      <span class="payment-detail-value balance">
        ¥{{ billDetail.balanceAmount.toFixed(2) }}
      </span>
    </div>
  </template>
</div>
```

- [ ] **Step 2: 添加辅助方法**

在 `<script setup>` 中添加：

```typescript
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/api/table'

// 获取支付方式标签
const getPaymentMethodLabel = (method: PaymentMethod): string => {
  return PAYMENT_METHOD_LABELS[method] || method
}

// 获取支付方式标签类型
const getPaymentMethodTagType = (method: PaymentMethod): string => {
  const typeMap: Record<PaymentMethod, string> = {
    offline: 'primary',
    online: 'success',
    balance: 'warning',
    combined: 'danger'
  }
  return typeMap[method] || 'info'
}
```

- [ ] **Step 3: 添加样式**

```scss
.payment-info-section {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.payment-method-display {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .payment-label {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
  }
}

.payment-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #dcdfe6;

  &:last-child {
    border-bottom: none;
  }
}

.payment-detail-label {
  font-size: 14px;
  color: #909399;
}

.payment-detail-value {
  font-size: 16px;
  font-weight: 600;

  &.balance {
    color: #e6a23c;
  }

  &.other {
    color: #409eff;
  }
}
```

- [ ] **Step 4: 提交修改**

```bash
git add frontend/src/components/BillDialog.vue
git commit -m "feat: 结账对话框显示支付明细"
```

---

## Chunk 7: 测试和文档

### Task 14: 记录修改文档

**Files:**
- Create: `docs/修改记录-2026-04-02-支付方式功能.md`

- [ ] **Step 1: 创建修改记录文档**

```markdown
# 修改记录 - 支付方式功能

**日期**: 2026-04-02
**功能**: 添加支付方式选择和处理

## 修改内容

### 数据库
- 订单表添加支付方式相关字段：`payment_method`, `balance_amount`, `other_payment_amount`

### 后端
- 实体类：Order添加支付方式字段
- DTO：StartTimerRequest、TableInfoResponse、BillResponse添加支付方式字段
- Service：开始计时保存支付方式，结账时处理余额扣除

### 前端
- API：添加支付方式类型定义和常量
- 组件：StartTimerDialog添加支付方式选择
- 组件：BillDialog显示支付明细

## 业务说明

- 开始计时不扣费，仅记录支付方式
- 结账时根据支付方式扣除会员余额
- 组合支付自动计算：优先使用余额，不足部分用线下
```

- [ ] **Step 2: 提交文档**

```bash
git add docs/修改记录-2026-04-02-支付方式功能.md
git commit -m "docs: 添加支付方式功能修改记录"
```

### Task 15: 手动测试验证

**Files:**
- N/A

- [ ] **Step 1: 测试非会员支付方式**

1. 启动后端和前端
2. 选择一个空闲桌台
3. 点击"开始计时"
4. 确认不选择会员，只显示"线下支付"和"线上支付"
5. 选择支付方式并确认开始
6. 验证桌台状态正常

- [ ] **Step 2: 测试会员余额支付**

1. 选择一个余额充足的会员
2. 选择"会员余额"支付方式
3. 确认显示余额信息
4. 开始计时
5. 使用一段时间后结账
6. 验证余额已正确扣除

- [ ] **Step 3: 测试组合支付**

1. 选择一个余额不足的会员（余额 < 套餐价格）
2. 选择"组合支付"支付方式
3. 确认显示余额和需补金额
4. 开始计时
5. 结账时验证：余额全部使用，剩余部分记为线下

- [ ] **Step 4: 测试余额不足提示**

1. 选择余额不足的会员
2. 选择"会员余额"支付（非组合）
3. 开始计时并结账
4. 验证正确提示余额不足

- [ ] **Step 5: 测试结账单显示**

1. 完成一次计时并结账
2. 在结账单中验证支付方式信息显示正确
3. 验证组合支付明细显示正确

---

## 完成检查清单

在完成所有任务后，确认以下事项：

- [ ] 数据库迁移已执行并验证
- [ ] 所有代码已提交到git
- [ ] 非会员支付方式正常工作
- [ ] 会员余额支付正常工作
- [ ] 组合支付计算正确
- [ ] 余额不足时正确提示
- [ ] 结账单显示支付明细
- [ ] 修改记录文档已创建
