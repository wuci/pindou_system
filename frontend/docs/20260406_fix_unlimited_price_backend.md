# 修改记录

## 日期
2026-04-06

## 问题描述
选择不限时套餐点击确定后，桌台卡片上显示的价格是7元（最低套餐价格），而不是不限时套餐的正确价格（68元）。

## 问题原因

1. **前端问题**：开始计时请求只传递 `presetDuration`（0），不传递套餐价格
2. **后端问题**：后端根据 `presetDuration` 计算价格，当 `presetDuration` 为 0 时无法区分用户选择的是哪个不限时套餐
3. **结果**：后端无法计算正确价格，使用默认最低价格或0元

## 解决方案

让前端传递套餐原价给后端，后端优先使用前端传递的价格。

### 后端修改

#### 1. `StartTimerRequest.java` - 添加原价字段

```java
@Schema(description = "预设时长（秒），0表示不设时长", example = "3600")
@NotNull(message = "预设时长不能为空")
@Min(value = 0, message = "预设时长不能小于0")
private Integer presetDuration;

@Schema(description = "套餐原价（元），前端传递给后端", example = "68.00")
private Double originalAmount;

@Schema(description = "订餐渠道", example = "store")
private String channel;
```

#### 2. `TableServiceImpl.java` - 优先使用前端传递的价格

```java
private AmountCalculationResult calculateInitialAmount(StartTimerRequest request) {
    Integer presetDuration = request.getPresetDuration() == 0 ? null : request.getPresetDuration();
    double originalAmount = 0.0;
    double finalAmount = 0.0;
    String discountId = null;
    BigDecimal discountRate = null;

    // 优先使用前端传递的原价
    if (request.getOriginalAmount() != null && request.getOriginalAmount() > 0) {
        originalAmount = request.getOriginalAmount();
        finalAmount = originalAmount;
        log.info("使用前端传递的原价: originalAmount={}", originalAmount);

        // 判断是否应用活动折扣
        if (request.getDiscountId() != null && !request.getDiscountId().isEmpty()) {
            // ... 活动折扣逻辑
        } else if (request.getMemberId() != null) {
            // ... 会员折扣逻辑
        }
    } else if (presetDuration != null) {
        // 原有的按时长计算价格逻辑（兼容性保留）
        String channel = request.getChannel() != null ? request.getChannel() : TableConstants.Channel.STORE;
        AmountDetail amountDetail = billingService.calculateAmount(channel, presetDuration, presetDuration);
        originalAmount = amountDetail.getTotalAmount();
        // ...
    }

    return new AmountCalculationResult(originalAmount, finalAmount, discountId, discountRate);
}
```

### 前端修改

#### 1. `table.ts` - API 接口添加原价字段

```typescript
export interface StartTableParams {
  presetDuration: number | null
  originalAmount?: number  // 套餐原价（元），前端传递给后端
  channel?: string  // 订餐渠道
  memberId?: number  // 会员ID
  remark?: string
  paymentMethod?: PaymentMethod  // 支付方式
  discountId?: string  // 活动折扣ID
}
```

#### 2. `StartTimerDialog.vue` - 提交时传递原价

```typescript
await startTable(props.table.id, {
  presetDuration: packageDuration,  // 只传套餐时长，不包含延长时间
  originalAmount: originalPrice.value,  // 传递套餐原价给后端
  channel: selectedChannel.value,
  memberId: selectedMember.value?.id,
  remark: form.value.remark,
  paymentMethod: selectedPaymentMethod.value,
  discountId: form.value.applyActivityDiscount ? form.value.discountId : undefined
})
```

## 修复效果

- ✅ 选择不限时套餐（68元）点击确定后，卡片显示68元
- ✅ 选择限时套餐（如30分钟7元）点击确定后，卡片显示7元
- ✅ 会员折扣和活动折扣仍然正常应用
- ✅ 兼容原有按时长计算价格的逻辑

## 技术要点

1. **前端计算价格**：套餐价格在前端根据用户选择确定
2. **后端信任前端**：后端优先使用前端传递的价格，避免重复计算导致的差异
3. **折扣应用**：后端仍负责计算会员折扣和活动折扣
4. **兼容性**：保留原有按时长计算价格的逻辑作为兜底

## 数据流

```
用户选择套餐
    ↓
前端显示套餐价格（originalPrice）
    ↓
点击确定，传递 originalAmount 给后端
    ↓
后端使用前端传递的原价
    ↓
后端计算折扣（会员/活动）
    ↓
保存订单并推送到前端
    ↓
卡片显示最终价格
```

## 注意事项

- 前端传递的是套餐**原价**（未折扣前的价格）
- 后端负责计算会员折扣和活动折扣
- 如果前端不传递 `originalAmount`，后端仍会按时长计算价格（兼容旧逻辑）

@author wuci
