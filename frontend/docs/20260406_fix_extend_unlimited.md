# 修改记录

## 日期
2026-04-06

## 问题描述
续费时不限时套餐的价格计算不正确。

## 问题原因

续费时，如果选择不限时套餐，`additionalDuration` 为 0，后端调用 `billingService.calculateAmount(channel, 0, 0)` 会按最低价格计算续费费用，导致价格不正确。

## 解决方案

修改后端续费逻辑，当 `additionalDuration` 为 0 时，使用不限时套餐价格进行计算。

### 修改内容

#### `TableServiceImpl.java` - 修改计算续费金额逻辑

```java
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
    }

    // ... 后续计算逻辑
}
```

## 续费逻辑说明

### 情况1：从限时套餐续费到限时套餐
- `additionalDuration` > 0
- 按续费时长计算续费费用
- `newPresetDuration = currentPresetDuration + additionalDuration`

### 情况2：从限时套餐续费到不限时套餐
- `additionalDuration` = 0
- 续费费用 = 不限时价格 - 当前订单原价
- `newPresetDuration = null`（改为不限时）

### 情况3：从不限时套餐续费到不限时套餐
- 当前已是 `presetDuration = null`
- 续费费用 = 0（不允许）
- 给出警告日志

### 情况4：从不限时套餐续费到限时套餐
- 当前 `presetDuration = null`
- `additionalDuration` > 0
- 按续费时长计算续费费用
- `newPresetDuration = additionalDuration`（改为限时套餐）

## 修复效果

- ✅ 从限时套餐续费到不限时套餐：正确计算差价
- ✅ 从限时套餐续费到限时套餐：按时长计算续费费用
- ✅ 从不限时套餐续费到限时套餐：按时长计算续费费用
- ✅ 从不限时套餐续费到不限时套餐：不允许，续费费用为0

## 注意事项

1. 当前订单已是不限时套餐时，不允许续费不限时套餐（续费费用为0）
2. 从限时套餐改为不限时套餐时，续费费用为两者的价格差
3. 如果找不到不限时套餐价格配置，续费费用为0

@author wuci
