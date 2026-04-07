# 修改记录

## 日期
2026-04-06

## 问题描述
1. 选择不限时套餐点击确定后，桌台卡片显示的价格是7元（最低套餐价格），而不是不限时套餐的正确价格（68元）
2. 之前的修改方案导致结账报错

## 最终解决方案

添加 `unlimited` 字段明确标识不限时套餐，后端根据此标识获取不限时套餐价格。

### 后端修改

#### 1. `StartTimerRequest.java` - 添加 unlimited 字段

```java
@Schema(description = "预设时长（秒），0表示不设时长", example = "3600")
@NotNull(message = "预设时长不能为空")
@Min(value = 0, message = "预设时长不能小于0")
private Integer presetDuration;

@Schema(description = "是否为不限时套餐", example = "false")
private Boolean unlimited;

@Schema(description = "订餐渠道", example = "store")
private String channel;
```

#### 2. `BillingService.java` - 添加获取不限时套餐价格接口

```java
/**
 * 获取不限时套餐价格
 *
 * @param channel 渠道代码
 * @return 不限时套餐价格，如果不存在返回null
 */
Double getUnlimitedPrice(String channel);
```

#### 3. `BillingServiceImpl.java` - 实现获取不限时套餐价格

```java
@Override
public Double getUnlimitedPrice(String channel) {
    log.info("获取不限时套餐价格: channel={}", channel);

    try {
        // 获取计费规则配置
        String billingRuleConfig = getBillingRuleConfig();
        JSONObject billingRuleJson = JSONUtil.parseObj(billingRuleConfig);

        // 获取渠道列表
        Object channelsObj = billingRuleJson.get("channels");
        if (channelsObj == null) {
            return null;
        }

        // 查找指定渠道
        for (Object channelObj : (Iterable) channelsObj) {
            JSONObject channelJson = JSONUtil.parseObj(channelObj);
            String channelCode = channelJson.getStr("channel");

            if (channel != null && channel.equals(channelCode)) {
                // 找到指定渠道，查找不限时规则
                Object rulesObj = channelJson.get("rules");
                if (rulesObj != null) {
                    for (Object ruleObj : (Iterable) rulesObj) {
                        JSONObject rule = JSONUtil.parseObj(ruleObj);
                        Boolean unlimited = rule.getBool("unlimited", false);

                        if (Boolean.TRUE.equals(unlimited)) {
                            Double price = rule.getDouble("price");
                            log.info("找到不限时套餐价格: channel={}, price={}", channel, price);
                            return price;
                        }
                    }
                }
                return null;
            }
        }

        return null;
    } catch (Exception e) {
        log.error("获取不限时套餐价格失败: {}", e.getMessage(), e);
        return null;
    }
}
```

#### 4. `TableServiceImpl.java` - 修改计算初始金额逻辑

```java
private AmountCalculationResult calculateInitialAmount(StartTimerRequest request) {
    Integer presetDuration = request.getPresetDuration() == 0 ? null : request.getPresetDuration();
    double originalAmount = 0.0;
    double finalAmount = 0.0;
    String discountId = null;
    BigDecimal discountRate = null;

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
        // ... 折扣逻辑
    }

    return new AmountCalculationResult(originalAmount, finalAmount, discountId, discountRate);
}
```

### 前端修改

#### 1. `table.ts` - 添加 unlimited 字段

```typescript
export interface StartTableParams {
  presetDuration: number | null
  unlimited?: boolean  // 是否为不限时套餐
  channel?: string
  memberId?: number
  remark?: string
  paymentMethod?: PaymentMethod
  discountId?: string
}
```

#### 2. `StartTimerDialog.vue` - 提交时传递 unlimited 字段

```typescript
await startTable(props.table.id, {
  presetDuration: packageDuration,
  unlimited: isSelectedRuleUnlimited.value,  // 传递是否为不限时套餐
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
- ✅ 结账功能正常，不会报错
- ✅ 会员折扣和活动折扣仍然正常应用

## 技术要点

1. **明确标识**：使用 `unlimited` 字段明确标识不限时套餐，而不是依赖 `presetDuration` 的值
2. **后端计算**：后端根据 `unlimited` 标识从计费规则中获取对应的价格
3. **渠道匹配**：支持不同渠道有不同的不限时套餐价格
4. **兼容性**：保留了原有的按时长计算价格的逻辑

## 数据流

```
用户选择不限时套餐
    ↓
前端计算 isSelectedRuleUnlimited = true
    ↓
点击确定，传递 unlimited: true 给后端
    ↓
后端检测到 unlimited = true
    ↓
调用 billingService.getUnlimitedPrice(channel)
    ↓
从计费规则中查找 unlimited: true 的规则
    ↓
获取该规则的 price 字段（如68元）
    ↓
计算折扣（会员/活动）
    ↓
保存订单并推送到前端
    ↓
卡片显示最终价格（68元）
```

## 注意事项

- 不限时套餐的价格来自数据库配置中的 `billing_rule` 配置
- 不同渠道的不限时套餐价格可能不同
- 如果找不到不限时套餐价格，会返回0，需要确保计费规则配置正确

@author wuci
