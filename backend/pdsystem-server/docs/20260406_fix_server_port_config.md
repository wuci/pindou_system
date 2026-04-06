# 修改记录

## 日期
2026-04-06

## 修改文件
- `src/main/resources/application.yml`

## 问题原因
配置文件中缺少服务器端口配置，且错误配置了 `context-path: /api`。

### 问题分析
1. 缺少 `server.port` 配置，导致使用默认端口 8080
2. 设置 `context-path: /api` 导致所有路径（包括静态资源）都加上 `/api` 前缀
3. 结果：静态资源无法访问，客户端地址 `http://localhost:9026/` 进不去

### 架构说明
- 前端静态文件：`http://localhost:9026/` (根路径)
- 后端API：`http://localhost:9026/api/*` (由 Controller @RequestMapping 控制)
- 不应使用 `server.servlet.context-path`，因为它会影响所有路径

## 修改内容

### 修改前
```yaml
# 缺少 server 配置
spring:
  # ...
```

### 修改后
```yaml
# 服务器配置
server:
  port: 9026

spring:
  # ...
```

## 验证结果
- ✅ Tomcat started on port(s): 9026 (http) with context path ''
- ✅ 客户端地址: http://localhost:9026/
- ✅ 接口文档: http://localhost:9026/api/doc.html
- ✅ Druid监控: http://localhost:9026/api/druid/

@author wuci
