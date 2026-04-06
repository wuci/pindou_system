# 修复 Nacos 配置加载问题

## 日期
2026-04-06

## 问题描述
1. 从 Nacos 读取配置时报错：
```
Failed to bind properties under 'logging' to java.util.Map<java.lang.String, java.lang.Object>
Property: logging
Value: ""
```
2. 配置源被加载但属性无法解析（如 `jwt.secret`）

## 原因分析
1. Nacos 中的 `common` 共享配置里，`logging` 属性格式不正确
2. Spring Cloud Alibaba Nacos 的 `shared-configs` 配置在解析 YAML 格式时可能存在问题

## 解决方案
1. 在 Nacos 配置中心的 `common` 配置中注释掉 `logging` 配置
2. 创建本地 `application.yml` 文件，将必要配置（数据源、Redis、JWT、业务配置）放在本地
3. 保留 `bootstrap.yml` 用于 Nacos 配置中心连接

## 修改文件
- `src/main/resources/bootstrap.yml`：Nacos 配置中心连接配置
- `src/main/resources/application.yml`：本地配置（数据源、Redis、JWT、业务配置）

## 最终配置
```yaml
# bootstrap.yml - Nacos 连接配置
spring:
  cloud:
    nacos:
      config:
        server-addr: 8.130.49.28:8848
        namespace: pd_system
        group: pd_system
        shared-configs:
          - data-id: common
            group: pd_system
            refresh: true
          - data-id: pd_server
            group: pd_system
            refresh: true
```

## 服务启动结果
- 服务成功启动在 `http://localhost:8080/api`
- 数据库连接正常
- Redis 连接正常
- Nacos 配置中心连接正常
