# 修改记录

## 日期
2026-04-06

## 问题描述
用户反馈选择"不限时"套餐时，费用信息不应该隐藏，需要显示不限时套餐的价格（如68元）。

## 修改内容

### 修改文件
`frontend/src/components/StartTimerDialog.vue`

### 修改说明

将费用信息的显示条件从 `v-if="!isSelectedRuleUnlimited"` 改为始终显示：

```html
<!-- 费用信息（始终显示） -->
<el-form-item label="费用">
  <!-- 选择会员时：显示原价和折扣价 -->
  <div v-if="selectedMember" class="price-display-row">
    <div class="price-item-inline">
      <span class="price-label">原价：</span>
      <span class="price-original">¥{{ originalPrice.toFixed(2) }}</span>
    </div>
    <div class="price-item-inline">
      <span class="price-label">折扣价：</span>
      <span class="price-discount">¥{{ discountedPrice.toFixed(2) }}</span>
      <span v-if="memberDiscount > 0" class="discount-tag">
        ({{ (memberDiscount * 10).toFixed(1) }}折)
      </span>
    </div>
  </div>
  <!-- 未选择会员时：只显示价格 -->
  <span v-else class="price-normal">¥{{ originalPrice.toFixed(2) }}</span>
</el-form-item>
```

## 修复效果

**选择限时套餐时**：
- ✅ 显示套餐时长预览
- ✅ 显示延长时间
- ✅ 显示总时长预览
- ✅ 显示费用（套餐价格）

**选择不限时套餐时**：
- ✅ 不显示套餐时长预览（显示"不设时长"）
- ✅ 不显示延长时间
- ✅ 不显示总时长预览
- ✅ **显示费用（不限时套餐价格，如¥68.00）**

## 显示逻辑总结

| 字段 | 限时套餐 | 不限时套餐 |
|------|---------|-----------|
| 套餐时长预览 | ✅ 显示 | ❌ 隐藏 |
| 延长时间 | ✅ 显示 | ❌ 隐藏 |
| 总时长预览 | ✅ 显示 | ❌ 隐藏 |
| 费用 | ✅ 显示 | ✅ 显示 |

## 注意事项

- 不限时套餐的价格来自数据库配置中的 `price` 字段
- 不同渠道的不限时套餐价格可能不同（工作日68元，节假日78元等）
- 会员折扣和活动折扣同样适用于不限时套餐

@author wuci
