# 会员管理功能 - 快速开始

## 环境准备

### 1. 数据库初始化

```bash
# 进入SQL脚本目录
cd D:\ai_project\pindouTimer\backend\sql\20260331

# 执行建表脚本（按顺序）
mysql -u root -p pindou_timer < add_member_level.sql
mysql -u root -p pindou_timer < add_member.sql
mysql -u root -p pindou_timer < update_order_member.sql
mysql -u root -p pindou_timer < init_member_levels.sql

# 验证表创建
mysql -u root -p pindou_timer -e "SHOW TABLES LIKE 'biz_member%';"
```

**预期输出**:
```
+---------------------------+
| Tables_in_pindou_timer     |
+---------------------------+
| biz_member                 |
| biz_member_level           |
+---------------------------+
```

### 2. 验证默认等级数据

```bash
mysql -u root -p pindou_timer -e "SELECT * FROM biz_member_level ORDER BY sort;"
```

**预期输出**:
```
+----+--------------+-------------+--------------+--------------+------+
| id | name         | min_amount  | max_amount  | discount_rate | sort |
+----+--------------+-------------+--------------+--------------+------+
|  1 | 豆豆萌新     |        0.00 |       300.00 |        0.950 |    1 |
|  2 | 熨烫能手     |      300.01 |      1000.00 |        0.900 |    2 |
|  3 | 像素匠人     |     1000.01 |      3000.00 |        0.850 |    3 |
|  4 | 熔豆典藏     |     3000.01 |         NULL |        0.800 |    4 |
+----+--------------+-------------+--------------+--------------+------+
```

---

## 后端启动

### 1. 编译项目

```bash
cd D:\ai_project\pindouTimer\backend
mvn clean install -DskipTests
```

### 2. 启动后端服务

```bash
cd pdsystem-server
mvn spring-boot:run
```

**预期日志**:
```
Started TimerApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 3. 验证后端API

**测试等级初始化**:
```bash
curl -X POST http://localhost:8080/api/member-levels/init \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**测试创建会员**:
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "测试用户",
    "phone": "13900139000",
    "address": "测试地址"
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "测试用户",
    "phone": "13900139000",
    "address": "测试地址",
    "totalAmount": 0.00,
    "levelId": 1,
    "levelName": "豆豆萌新",
    "discountRate": 0.950,
    "createdAt": 1730374800000,
    "updatedAt": 1730374800000
  }
}
```

---

## 前端启动

### 1. 安装依赖

```bash
cd D:\ai_project\pindouTimer\frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

**预期输出**:
```
VITE v5.0.0  ready in XXX ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 3. 访问系统

1. 打开浏览器访问：http://localhost:5173
2. 使用管理员账号登录
3. 进入"会员管理"页面

---

## 功能验证

### 场景1: 创建会员

1. 点击"会员管理"菜单
2. 点击"新增会员"按钮
3. 填写表单：
   - 会员名称：张三
   - 手机号：13800138000
   - 地址：北京市朝阳区（可选）
4. 点击"确定"

**预期结果**：
- ✅ 会员创建成功
- ✅ 默认等级为"豆豆萌新"
- ✅ 累计消费为0元

### 场景2: 会员等级升级

1. 通过API或数据库直接修改会员累计消费：
   ```sql
   UPDATE biz_member SET total_amount = 500.00 WHERE id = 1;
   ```
2. 刷新会员列表

**预期结果**：
- ✅ 会员等级自动变为"熨烫能手"
- ✅ 折扣率变为9折

### 场景3: 应用会员折扣

1. 选择一个桌台开始计时（选择30分钟套餐，11元）
2. 使用一段时间后点击"结账"
3. 在结账对话框中选择会员（张三）
4. 查看折扣信息

**预期结果**：
- ✅ 显示会员等级：熨烫能手（9折）
- ✅ 显示原价：11.00元
- ✅ 显示折扣：-1.10元
- ✅ 显示实付：9.90元
5. 点击"确认结账"
6. 检查会员累计消费是否增加

### 场景4: 订单完成后累计消费增加

1. 完成上一笔订单（状态：completed）
2. 查看会员详情

**预期结果**：
- ✅ 会员累计消费增加11元
- ✅ 等级保持不变（因为300 < 500 < 1000）

### 场景5: 跨等级升级

1. 通过API或数据库修改会员累计消费为2500元
2. 创建多笔订单累计消费超过500元
3. 完成所有订单

**预期结果**：
- ✅ 每笔订单完成后累计消费累加
- ✅ 当累计消费超过3000元时，等级升级为"像素匠人"
- ✅ 当累计消费超过3000.01元时，等级升级为"熔豆典藏"

---

## 故障排查

### 问题1: 数据库表创建失败

**检查**:
```bash
mysql -u root -p pindou_timer -e "SHOW TABLES;"
```

**解决**:
- 确认数据库存在：`CREATE DATABASE IF NOT EXISTS pindou_timer`
- 确认SQL脚本路径正确
- 查看MySQL错误日志

### 问题2: 后端启动失败

**检查**:
```bash
# 查看端口占用
netstat -ano | findstr :8080

# 查看Java进程
jps -l
```

**解决**:
- 更改application.yml中的端口配置
- 停止占用8080端口的进程
- 清理Maven缓存：`mvn clean`

### 问题3: 前端API请求失败

**检查**:
1. 浏览器开发者工具 → Network标签
2. 查看请求响应

**常见错误**:
- 401 Unauthorized → Token过期，重新登录
- 404 Not Found → API路径错误，检查Controller路径
- 500 Server Error → 查看后端日志

### 问题4: 手机号唯一性校验不生效

**检查**:
```bash
mysql -u root -p pindou_timer -e "SHOW CREATE TABLE biz_member\G"
```

**确认**:
- `uk_phone` 唯一索引存在
- 手机号字段长度足够（VARCHAR(20)）
- 后端validatePhoneUnique方法被调用

---

## 性能测试

### 并发测试会员等级升级

```bash
# 使用Apache Bench测试
ab -n 1000 -c 10 \
   -H "Authorization: Bearer YOUR_TOKEN" \
   -H "Content-Type: application/json" \
   -p 'member_id=1&original_amount=100' \
   http://localhost:8080/api/orders/complete
```

**验证**:
- 累计消费最终正确（100000元）
- 等级正确升级
- 无数据不一致

---

## 下一步

完成快速开始验证后，可以：

1. **开发模式**: 按照tasks.md执行完整开发流程
2. **测试模式**: 执行完整的测试用例
3. **生产部署**: 参考部署文档进行生产环境配置

---

## 附录：常用命令

### 数据库

```bash
# 查看所有会员
mysql -u root -p pindou_timer -e "SELECT * FROM biz_member;"

# 查看所有等级
mysql -u root -p pindou_timer -e "SELECT * FROM biz_member_level ORDER BY sort;"

# 修改会员累计消费
mysql -u root -p pindou_timer -e "UPDATE biz_member SET total_amount = 500 WHERE id = 1;"

# 重置默认等级
mysql -u root -p pindou_timer -e "DELETE FROM biz_member_level WHERE id > 4;"
```

### 后端

```bash
# 重新编译
cd D:\ai_project\pindouTimer\backend
mvn clean compile

# 跳过测试编译
mvn clean install -DskipTests

# 打包
mvn clean package -DskipTests
```

### 前端

```bash
# 安装新依赖
cd D:\ai_project\pindouTimer\frontend
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```
