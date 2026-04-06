# 豆屿温柔集管理系统 - 数据库初始化脚本

**版本：** 1.0
**日期：** 2026-04-03
**数据库：** MySQL 8.0+

## 📁 文件说明

本目录包含两个SQL脚本文件：

### 1. `01_建表脚本.sql`
- 创建数据库 `pindou_timer`
- 创建所有表结构（12张表）
- 创建所有外键约束和索引
- **支持重复执行**（使用 DROP TABLE IF EXISTS）

### 2. `02_初始化数据脚本.sql`
- 插入预设角色数据（4个角色）
- 插入预设用户数据（3个用户）
- 插入会员等级数据（4个等级）
- 插入桌台分类数据（3个分类）
- 插入桌台数据（20个桌台）
- 插入布局配置数据（4个配置）
- 插入系统配置数据（4个配置）
- 插入示例会员数据（3个会员）
- **支持重复执行**（使用 INSERT IGNORE）

## 📊 数据库表结构

### 系统管理表（4张）
| 表名 | 说明 |
|-----|------|
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_operation_log` | 操作日志表 |
| `sys_config` | 系统配置表 |

### 业务表（8张）
| 表名 | 说明 |
|-----|------|
| `biz_member_level` | 会员等级配置表 |
| `biz_member` | 会员表 |
| `biz_table_category` | 桌台分类表 |
| `biz_table_layout_config` | 桌台布局配置表 |
| `biz_table` | 桌台表 |
| `biz_order` | 订单表 |
| `biz_recharge_record` | 充值记录表 |
| `biz_consumption_record` | 消费记录表 |

## 🚀 使用步骤

### 方式一：命令行执行

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 执行建表脚本
source D:/ai_project/pindouTimer/backend/sql/20260403/01_建表脚本.sql

# 3. 执行初始化数据脚本
source D:/ai_project/pindouTimer/backend/sql/20260403/02_初始化数据脚本.sql
```

### 方式二：直接在MySQL客户端执行

```sql
-- 1. 执行建表脚本
source D:/ai_project/pindouTimer/backend/sql/20260403/01_建表脚本.sql;

-- 2. 执行初始化数据脚本
source D:/ai_project/pindouTimer/backend/sql/20260403/02_初始化数据脚本.sql;
```

### 方式三：使用Navicat或其他工具

1. 打开Navicat
2. 连接到MySQL数据库
3. 新建查询
4. 分别打开并执行两个SQL文件

## 👤 预设账号

系统初始化后，会创建以下预设账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 拥有所有权限 |
| manager | 123456 | 店长 | 管理权限（不含角色管理） |
| staff | 123456 | 店员 | 操作权限 |

## 👥 示例会员

系统会创建3个示例会员用于测试：

| 会员 | 手机号 | 等级 | 余额 | 累计消费 |
|-----|-------|------|------|---------|
| 张三 | 13800138000 | 普通会员 | ¥500.00 | ¥1,500.00 |
| 李四 | 13900139000 | 银卡会员 | ¥1,000.00 | ¥5,000.00 |
| 王五 | 13700137000 | 金牌会员 | ¥2,000.00 | ¥15,000.00 |

## 🔄 重复执行支持

两个SQL脚本均支持重复执行，不会报错：

### 建表脚本
- 使用 `DROP TABLE IF EXISTS` 删除已存在的表
- 使用 `CREATE DATABASE IF NOT EXISTS` 创建数据库
- 可以安全地重复执行

### 初始化数据脚本
- 使用 `INSERT IGNORE` 插入数据
- 如果数据已存在则跳过，不会报错
- 可以安全地重复执行

## ⚠️ 注意事项

1. **执行顺序**：必须先执行建表脚本，再执行初始化数据脚本
2. **字符集**：使用 `utf8mb4` 字符集，支持中文和表情符号
3. **外键约束**：建表时会自动创建外键约束
4. **数据清理**：如需重新初始化，直接重复执行即可

## 📝 验证安装

执行完成后，可以运行以下SQL验证：

```sql
-- 验证表是否创建成功
USE pindou_timer;
SHOW TABLES;

-- 验证数据是否插入成功
SELECT COUNT(*) as role_count FROM sys_role;
SELECT COUNT(*) as user_count FROM sys_user;
SELECT COUNT(*) as table_count FROM biz_table;
SELECT COUNT(*) as member_count FROM biz_member;
SELECT COUNT(*) as member_level_count FROM biz_member_level;
```

预期结果：
- role_count: 4
- user_count: 3
- table_count: 20
- member_count: 3
- member_level_count: 4

## 🔧 故障排除

### 问题1：外键约束失败
**原因**：表删除顺序不正确
**解决**：脚本已使用 `SET FOREIGN_KEY_CHECKS = 0` 来避免此问题

### 问题2：字符集问题
**原因**：MySQL版本不支持 utf8mb4
**解决**：确保使用 MySQL 5.7+ 版本

### 问题3：权限不足
**原因**：当前用户没有创建数据库的权限
**解决**：使用有管理员权限的账号执行

## 📞 联系方式

如有问题，请联系开发团队。

**开发者：** wuci
**日期：** 2026-04-03
