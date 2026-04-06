# DiscountService 空指针修复记录

**日期**: 2026-04-06
**作者**: wuci
**修复原因**: 代码存在空指针风险

## 问题描述

原代码中存在多处空指针风险，例如：
```java
if (request.getType() == DiscountType.MEMBER && request.getMemberLevelId() == null) {
    throw new BusinessException(ErrorCode.BAD_REQUEST, "会员折扣类型必须指定会员等级");
}
```

当 `request.getType()` 为 null 时，会抛出 `NullPointerException`。

## 修复方案

### 1. 类型比较改为使用 intValue()
```java
// 修复前
if (request.getType() == DiscountType.MEMBER && request.getMemberLevelId() == null) {

// 修复后
if (request.getType() != null && request.getType().intValue() == DiscountType.MEMBER && request.getMemberLevelId() == null) {
```

### 2. 修复的位置
- `createDiscount` 方法
- `updateDiscount` 方法
- `calculateDiscount` 方法

### 3. getTypeName 方法同样修复
```java
// 修复前
switch (type) {
    case DiscountType.FIXED:
        return "固定折扣";

// 修复后
int typeValue = type.intValue();
switch (typeValue) {
    case DiscountType.FIXED:
        return "固定折扣";
```

## 修改文件

1. `pdsystem-service/service/impl/DiscountServiceImpl.java`
2. `pdsystem-service/service/impl/MemberServiceImpl.java` - 修复 `request.getOriginalAmount()` 改为 `request.getAmount()`
3. `pdsystem-service/service/impl/TableServiceImpl.java` - 修复 `discountRequest.setOriginalAmount()` 改为 `discountRequest.setAmount()`

## 相关问题修复

同时修复了以下问题：
1. `CalculateDiscountRequest` 字段从 `originalAmount` 改为 `amount`
2. `CalculateDiscountResponse` 新增 `appliedDiscountName` 字段
3. 所有使用该 DTO 的地方都进行了相应更新
