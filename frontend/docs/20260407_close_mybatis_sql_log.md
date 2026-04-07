# 修改记录

## 日期
2026-04-07

## 问题描述
后台控制台打印大量 MyBatis SQL 日志，包括：
- SqlSession was not registered for synchronization
- JDBC Connection will not be managed by Spring
- Preparing/Parameters 等SQL执行信息

## 解决方案
注释掉 MyBatis-Plus 的 SQL 日志实现配置。

### 修改内容

#### application.yml

**修改前：**
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

**修改后：**
```yaml
mybatis-plus:
  configuration:
    # log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 已关闭SQL日志打印
```

## 效果

- ✅ 不再打印 SQL 执行日志
- ✅ 不再打印 JDBC Connection 管理信息
- ✅ 控制台输出更清爽

## 注意事项

如果需要调试 SQL，可以临时取消注释该配置来查看 SQL 执行情况。

@author wuci
