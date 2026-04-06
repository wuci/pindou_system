# 豆屿温柔集

## 项目简介

豆屿温柔集是一个为手工制作体验店设计的Web端管理系统，支持多店员登录、角色权限控制、桌台计时计费、订单管理、操作日志记录和超时提醒功能。

## 技术栈

### 后端
- Spring Boot 2.7.18
- JDK 1.8
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis 6.x
- JWT 0.11.5
- Knife4j 4.1.0

### 前端
- Vue 3.4+
- Vite 5.x
- TypeScript
- Element Plus
- Pinia
- Vue Router 4.x
- Axios
- ECharts

## 项目结构

```
pindou-timer/
├── backend/                    # 后端项目
│   ├── sql/                      # 数据库脚本
│   │   ├── schema.sql           # 建表脚本
│   │   ├── data.sql             # 初始数据
│   │   ├── init_database.bat    # Windows初始化脚本
│   │   ├── init_database.sh     # Linux/Mac初始化脚本
│   │   └── README.md            # SQL脚本说明
│   ├── src/
│   │   ├── java/com/pindou/timer/
│   │   │   ├── common/          # 公共模块
│   │   │   │   ├── result/      # 响应结果
│   │   │   │   ├── exception/    # 异常处理
│   │   │   │   ├── util/         # 工具类
│   │   │   │   ├── config/       # 配置类
│   │   │   │   └── modules/      # 业务模块
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 组件
│   │   ├── composables/       # 组合式函数
│   │   ├── layouts/           # 布局
│   │   ├── router/            # 路由
│   │   ├── stores/            # 状态管理
│   │   ├── types/             # 类型定义
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面
│   │   ├── App.vue
│   │   └── main.ts
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
│
└── docx/                       # 文档目录
```

## 快速开始

### 环境要求

- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.x
- Maven 3.6+

### 后端启动

1. 快速初始化数据库

**Windows用户：**
```bash
# 方式一：双击运行批处理脚本
cd backend\sql
init_database.bat

# 方式二：命令行执行
cd backend\sql
init_database.bat
```

**Linux/Mac用户：**
```bash
cd backend/sql
chmod +x init_database.sh
./init_database.sh
```

**手动初始化：**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE pindou_timer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
mysql -u root -p pindou_timer < backend/sql/schema.sql
mysql -u root -p pindou_timer < backend/sql/data.sql
```

2. 配置数据库连接
```bash
# 编辑 backend/src/main/resources/application.yml
# 修改数据库用户名和密码
```

3. 启动后端
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务启动在 `http://localhost:8080/api`

### 前端启动

1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端服务启动在 `http://localhost:3000`

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | admin | 123456 | 系统最高权限 |
| 店长 | manager | 123456 | 管理权限 |
| 店员 | staff | 123456 | 操作权限 |

## Day 1 完成内容

### 后端 ✅
- [x] SpringBoot项目结构搭建
- [x] Maven依赖配置（pom.xml）
- [x] 数据库连接配置（Druid）
- [x] 数据库建表SQL脚本（6张表）
- [x] 初始数据SQL脚本（预设角色、用户、桌台、配置）
- [x] 统一响应格式类
- [x] 全局异常处理器
- [x] JWT工具类
- [x] 密码工具类（BCrypt）
- [x] Redis工具类
- [x] Redis配置类
- [x] Web MVC配置（CORS）

### 前端 ✅
- [x] Vue3项目结构搭建
- [x] Vite配置
- [x] TypeScript配置
- [x] Element Plus集成
- [x] Axios请求封装
- [x] Vue Router配置
- [x] Pinia状态管理配置
- [x] 登录页面UI
- [x] 默认布局组件
- [x] 工作台首页
- [x] 其他页面占位

### 数据库 ✅
- [x] 用户表
- [x] 角色表
- [x] 桌台表
- [x] 订单表
- [x] 操作日志表
- [x] 系统配置表
- [x] 预设角色数据（4个角色）
- [x] 预设用户数据（3个用户）
- [x] 预设桌台数据（20个桌台）
- [x] 预设配置数据

## 接口文档

启动后端服务后，访问：
- Knife4j文档：http://localhost:8080/api/doc.html
- Druid监控：http://localhost:8080/api/druid/（admin/admin）

## 开发进度

- [x] Day 1: 项目初始化 + 基础架构
- [ ] Day 2: 认证授权模块
- [ ] Day 3: 桌台管理模块
- [ ] Day 4: 订单管理 + 计时计费
- [ ] Day 5: 结账流程
- [ ] Day 6-10: 管理功能开发
- [ ] Day 11-15: 优化与交付

## License

MIT License
