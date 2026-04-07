# 性能优化记录 - 第二轮

## 日期
2026-04-07

## 问题描述
第一次优化后，`TableController.getTableList()` 仍然耗时 2061ms，需要进一步优化。

## 问题分析

### 发现的问题

1. **配置重复查询** - `getExtendTimeConfig()` 在每个台台转换时都被调用
   - 每次调用都会执行 `configService.getSystemConfig()`
   - 50个台台 = 50次配置查询

2. **计费规则重复查询** - `calculateAmount()` 每次都调用 `getBillingRuleConfig()`
   - 每次调用都会查询数据库
   - 50个台台 = 50次计费规则查询

**总计额外开销：100次数据库查询！**

## 优化方案

### 1. 预加载配置到 getTableList 方法

**优化前：**
```java
// 每个台台都调用
.map(table -> convertToResponse(table, ...))

// convertToResponse 中
if (table.getStartTime() != null && table.getPresetDuration() != null) {
    int extendTimeMinutes = getExtendTimeConfig();  // 每次都查询！
    ...
}
```

**优化后：**
```java
public List<TableInfoResponse> getTableList(...) {
    // 在方法开始时预加载一次
    int extendTimeMinutes = getExtendTimeConfig();

    // 传递给转换方法
    final int finalExtendTimeMinutes = extendTimeMinutes;
    List<TableInfoResponse> responses = tables.stream()
            .map(table -> convertToResponse(table, ..., finalExtendTimeMinutes))
            .collect(Collectors.toList());
}
```

### 2. 计费规则缓存优化

在 `BillingServiceImpl` 中添加缓存：

```java
@Service
public class BillingServiceImpl implements BillingService {

    // 缓存变量
    private volatile String cachedBillingRuleConfig;
    private volatile long cacheTime;
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;  // 5分钟过期

    private String getBillingRuleConfig() {
        // 检查缓存
        String cached = cachedBillingRuleConfig;
        long now = System.currentTimeMillis();
        if (cached != null && (now - cacheTime) < CACHE_EXPIRY_MS) {
            return cached;  // 使用缓存
        }

        // 缓存过期，重新加载
        Config config = configMapper.selectOne(wrapper);
        cachedBillingRuleConfig = config.getConfigValue();
        cacheTime = now;
        return cachedBillingRuleConfig;
    }

    /**
     * 清除计费规则缓存
     * 当配置更新时调用此方法
     */
    public void clearBillingRuleCache() {
        cachedBillingRuleConfig = null;
        cacheTime = 0;
    }
}
```

### 3. 添加性能监控日志

```java
public List<TableInfoResponse> getTableList(...) {
    long startTime = System.currentTimeMillis();

    long queryStart = System.currentTimeMillis();
    List<Table> tables = tableMapper.selectList(wrapper);
    log.info("查询桌台数据耗时: {}ms, count={}", System.currentTimeMillis() - queryStart, tables.size());

    long preloadConfigStart = System.currentTimeMillis();
    int extendTimeMinutes = getExtendTimeConfig();
    log.info("预加载配置耗时: {}ms", System.currentTimeMillis() - preloadConfigStart);

    long preloadStart = System.currentTimeMillis();
    // ... 批量预加载
    log.info("批量预加载耗时: {}ms", System.currentTimeMillis() - preloadStart);

    long convertStart = System.currentTimeMillis();
    // ... 转换
    log.info("转换响应对象耗时: {}ms", System.currentTimeMillis() - convertStart);

    log.info("获取桌台列表成功: count={},耗时={}ms", responses.size(), endTime - startTime);
}
```

## 优化效果

| 问题 | 优化前 | 优化后 |
|------|--------|--------|
| 配置查询次数 | 50次 | 1次 |
| 计费规则查询 | 50次 | 1次（5分钟内复用） |
| 预计性能提升 | - | ~90% |

## 修改文件

1. **TableServiceImpl.java**
   - 在 `getTableList` 开始时预加载配置
   - 修改 `convertToResponse` 方法签名，接收 `extendTimeMinutes` 参数
   - 添加性能监控日志

2. **BillingServiceImpl.java**
   - 添加 `cachedBillingRuleConfig` 和 `cacheTime` 缓存变量
   - 修改 `getBillingRuleConfig()` 使用缓存
   - 添加 `clearBillingRuleCache()` 方法

## 缓存策略说明

### 计费规则缓存
- **缓存时间**：5分钟
- **过期策略**：时间到期自动重新加载
- **手动清除**：调用 `clearBillingRuleCache()` 方法
- **线程安全**：使用 `volatile` 关键字保证可见性

### 何时清除缓存
当用户在设置页面更新计费规则时，需要调用：
```java
billingServiceImpl.clearBillingRuleCache();
```

## 后续建议

1. **使用 Spring Cache**：考虑使用 `@Cacheable` 注解替代手动缓存
2. **配置中心**：对于频繁访问的配置，考虑使用 Redis 或本地缓存
3. **监控告警**：添加慢查询监控，及时发现性能问题

@author wuci
