# 会员管理功能 - 实现计划

**功能**: 会员管理系统
**日期**: 2026-03-31
**版本**: 1.0

---

## Technical Context

### Tech Stack

**后端**:
- Java 17+
- Spring Boot 2.7+
- MyBatis Plus 3.5+
- MySQL 8.0
- Maven

**前端**:
- Vue 3.3+
- TypeScript 5.0+
- Element Plus 2.4+
- Vite 5.0+
- Pinia (状态管理)

**构建工具**:
- 后端: Maven
- 前端: Vite

### Project Structure

**后端模块结构**:
```
backend/
├── pdsystem-entity/          # 实体类和DTO
│   └── src/main/java/com/pindou/timer/
│       ├── entity/           # 实体类
│       └── dto/              # 数据传输对象
├── pdsystem-mapper/          # Mapper接口
├── pdsystem-service/         # Service层
│   └── src/main/java/com/pindou/timer/service/
│       ├── impl/             # Service实现
│       └── *.java            # Service接口
├── pdsystem-server/          # Controller层
│   └── src/main/java/com/pindou/timer/controller/
├── pdsystem-common/          # 公共模块
│   └── src/main/java/com/pindou/timer/common/
│       ├── result/           # 统一响应
│       └── exception/        # 异常处理
└── sql/                      # SQL脚本
```

**前端结构**:
```
frontend/
├── src/
│   ├── api/                  # API接口
│   ├── components/           # 公共组件
│   ├── views/                # 页面组件
│   │   ├── MemberManagement/
│   │   └── Settings/
│   ├── router/               # 路由配置
│   ├── stores/               # Pinia状态管理
│   └── types/                # TypeScript类型定义
```

### Dependencies

**现有依赖（已集成）**:
- Spring Boot Web
- MyBatis Plus
- Lombok
- Hutool (工具库)
- Vue 3 + Element Plus

**新增依赖（会员功能）**:
- 无新增依赖（使用现有技术栈）

### Integration Points

1. **Order服务**: 订单完成后触发会员累计消费更新
2. **Table服务**: 结账时应用会员折扣
3. **Config服务**: 会员等级配置管理（复用现有Config服务）

---

## Constitution Check

**注意**: 项目未找到 `constitution.md`，使用通用原则：

### 代码质量标准

1. **代码风格**:
   - Java: 遵循阿里巴巴Java开发手册
   - TypeScript: 遵循Vue官方风格指南
   - 使用Lombok减少样板代码
   - 使用Swagger文档注解

2. **异常处理**:
   - 使用BusinessException抛出业务异常
   - 统一错误码（ErrorCode枚举）
   - 前端使用ElMessage显示错误信息

3. **数据验证**:
   - 后端使用@Valid注解 + JSR303验证
   - 前端表单验证规则
   - 手机号正则: `^1[3-9]\\d{9}$`

4. **事务管理**:
   - Service层方法使用@Transactional
   - 会员累计消费更新必须事务保证

### 架构原则

1. **分层架构**: Controller → Service → Mapper，严格遵守分层
2. **单一职责**: 每个Service专注一个领域
3. **接口隔离**: DTO专门用于数据传输，不暴露实体
4. **依赖注入**: 使用Spring依赖注入，避免硬编码

### 安全原则

1. **权限控制**: 会员管理功能需要相应权限
2. **数据校验**: 前后端双重校验
3. **SQL注入防护**: 使用MyBatis Plus防注入
4. **并发控制**: 会员等级升级使用数据库锁或乐观锁

---

## Phase 0: Research & Clarifications

### Research Topics

鉴于项目已有类似功能实现（用户管理、角色管理、桌台管理），这些模式可以复用：

1. ✅ **实体类设计模式**: 参考 User.java, Role.java
2. ✅ **Service层模式**: 参考 UserServiceImpl, RoleServiceImpl
3. ✅ **Controller层模式**: 参考 UserController, RoleController
4. ✅ **前端列表页面模式**: 参考 UserManagement, RoleManagement
5. ✅ **对话框模式**: 参考 StartTimerDialog, BillDialog

### No Clarifications Needed

所有技术选择和实现模式都有现有代码可参考。

---

## Phase 1: Data Model

### Entities

#### 1. Member (会员实体)

```java
@Data
@TableName("biz_member")
public class Member {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;                    // 会员名称
    private String phone;                   // 手机号（唯一）
    private String address;                 // 地址（可选）
    private BigDecimal totalAmount;         // 累计消费金额
    private Long levelId;                   // 会员等级ID（外键）

    private Long createdAt;
    private Long updatedAt;
}
```

**验证规则**:
- name: 非空，1-50字符
- phone: 非空，符合手机号格式，唯一
- address: 可选，最大200字符
- totalAmount: 默认0
- levelId: 非空，外键约束

#### 2. MemberLevel (会员等级实体)

```java
@Data
@TableName("biz_member_level")
public class MemberLevel {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;                    // 等级名称
    private BigDecimal minAmount;           // 最小累计金额
    private BigDecimal maxAmount;           // 最大累计金额（null=无上限）
    private BigDecimal discountRate;        // 折扣率（0.9=9折）
    private Integer sort;                   // 排序

    private Long createdAt;
    private Long updatedAt;
}
```

**验证规则**:
- name: 非空，1-50字符
- minAmount: 非空，≥0
- maxAmount: 可选，> minAmount或null
- discountRate: 非空，0.1-1.0
- sort: 默认0

### Relationships

```
Member (N) -----> (1) MemberLevel
Order (N) -----> (0/1) Member
```

### State Transitions

**会员等级升级**:
```
累计消费变化 → 计算新等级 → 等级变化时更新level_id
```

---

## Phase 1: Contracts

### API Contracts

#### 1. 会员管理 API

**创建会员**
```
POST /api/members
Request: {
  name: string,
  phone: string,
  address?: string
}
Response: {
  id: number,
  name: string,
  phone: string,
  address: string,
  totalAmount: number,
  levelId: number,
  levelName: string,
  discountRate: number,
  createdAt: number
}
```

**会员列表**
```
GET /api/members?page=1&pageSize=10&keyword=xxx
Response: {
  list: MemberResponse[],
  total: number,
  page: number,
  pageSize: number
}
```

**搜索会员**
```
GET /api/members/search?keyword=xxx
Response: MemberResponse[]
```

#### 2. 会员等级 API

**获取所有等级**
```
GET /api/member-levels
Response: MemberLevelResponse[]
```

**创建等级**
```
POST /api/member-levels
Request: {
  name: string,
  minAmount: number,
  maxAmount?: number,
  discountRate: number,
  sort: number
}
```

#### 3. 折扣计算 API

**计算折扣**
```
POST /api/members/{id}/discount
Request: {
  originalAmount: number
}
Response: {
  originalAmount: number,
  discountRate: number,
  discountAmount: number,
  finalAmount: number
}
```

---

## Phase 1: Quickstart

### 开发环境准备

1. **数据库准备**:
   ```bash
   # 执行建表脚本
   mysql -u root -p pindou_timer < backend/sql/20260331/add_member_level.sql
   mysql -u root -p pindou_timer < backend/sql/20260331/add_member.sql
   mysql -u root -p pindou_timer < backend/sql/20260331/update_order_member.sql
   mysql -u root -p pindou_timer < backend/sql/20260331/init_member_levels.sql
   ```

2. **后端启动**:
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run -pl pdsystem-server
   ```

3. **前端启动**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### 验证步骤

**后端验证**:
```bash
# 1. 初始化默认等级
curl -X POST http://localhost:8080/api/member-levels/init

# 2. 查询等级
curl http://localhost:8080/api/member-levels

# 3. 创建会员
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","phone":"13800138000"}'

# 4. 搜索会员
curl "http://localhost:8080/api/members/search?keyword=138"
```

**前端验证**:
1. 访问 http://localhost:5173/members
2. 点击"新增会员"创建测试会员
3. 在设置页面配置会员等级
4. 测试会员等级自动分配

---

## Implementation Phases

### Phase 1: 数据库和实体 (1-2天)

**目标**: 创建数据库表、实体类、DTO

**任务**:
1. 创建4个SQL脚本
2. 创建2个实体类
3. 创建10个DTO类
4. 更新Order实体和Order相关DTO

**验收标准**:
- [ ] 数据库表创建成功，约束生效
- [ ] 实体类Lombok注解正确
- [ ] DTO验证注解完整
- [ ] 编译无错误

---

### Phase 2: 后端基础 - Service层 (2-3天)

**目标**: 实现会员和会员等级的Service层

**任务**:
1. 实现MemberLevelService (查询、初始化、根据金额查询等级)
2. 实现MemberService (CRUD、手机号唯一性、等级计算、折扣计算)
3. 添加ErrorCode枚举值

**验收标准**:
- [ ] 可以初始化4个默认等级
- [ ] 可以根据累计消费金额正确计算等级
- [ ] 手机号唯一性校验生效
- [ ] 折扣计算精度正确（保留2位小数）

---

### Phase 3: 后端Controller层 (1-2天)

**目标**: 实现Controller接口

**任务**:
1. 实现MemberController (6个接口)
2. 实现MemberLevelController (5个接口)

**验收标准**:
- [ ] 所有接口可正常访问
- [ ] 参数验证生效
- [ ] 异常处理正确
- [ ] 返回数据格式统一

---

### Phase 4: 前端API层 (1天)

**目标**: 创建前端API接口和类型定义

**任务**:
1. 创建member.ts API文件
2. 创建memberLevel.ts API文件
3. 创建TypeScript类型定义
4. 更新table.ts API支持会员参数

**验收标准**:
- [ ] TypeScript类型定义完整
- [ ] API接口封装正确
- [ ] 错误处理统一

---

### Phase 5: 前端页面开发 (3-4天)

**目标**: 实现会员管理和等级配置页面

**任务**:
1. 创建MemberManagement页面
2. 创建MemberDialog组件
3. 创建MemberLevelConfig组件
4. 配置路由和菜单

**验收标准**:
- [ ] 会员列表正常显示
- [ ] 新增/编辑/删除功能正常
- [ ] 搜索功能正常
- [ ] 分页功能正常
- [ ] 会员等级配置正常

---

### Phase 6: 结账集成 (2-3天)

**目标**: 改造结账对话框，集成会员折扣

**任务**:
1. 修改BillDialog组件（会员选择、折扣显示）
2. 修改endTable接口支持会员ID
3. 修改getBill接口返回会员信息
4. 后端TableService支持会员折扣

**验收标准**:
- [ ] 结账对话框可以选择会员
- [ ] 显示会员等级和折扣率
- [ ] 正确计算折扣金额
- [ ] 订单保存会员关联

---

### Phase 7: 累计消费和等级升级 (2天)

**目标**: 实现订单完成后更新累计消费和等级自动升级

**任务**:
1. MemberService实现updateTotalAmount方法
2. TableService在endTimer时调用会员更新
3. 添加等级升级日志（可选）

**验收标准**:
- [ ] 已完成订单正确累加累计消费
- [ ] 已作废订单不累加
- [ ] 跨等级时自动升级
- [ ] 并发安全

---

### Phase 8: 订单显示会员信息 (1天)

**目标**: 订单列表和详情显示会员相关信息

**任务**:
1. 更新OrderInfoResponse包含会员信息
2. 更新OrderDetailResponse包含会员信息
3. 前端订单列表显示会员列
4. 前端订单详情显示会员信息

**验收标准**:
- [ ] 订单列表显示会员姓名
- [ ] 订单详情显示原价、折扣、实付
- [ ] 折扣信息完整

---

### Phase 9: 权限配置 (1天)

**目标**: 添加会员管理相关权限

**任务**:
1. 更新角色权限枚举
2. 在角色管理中配置会员权限
3. Controller接口添加权限注解

**验收标准**:
- [ ] 无权限无法访问会员管理
- [ ] 权限配置持久化

---

### Phase 10: 集成测试和文档 (1-2天)

**目标**: 端到端测试和文档编写

**任务**:
1. 编写测试用例
2. 执行完整流程测试
3. 编写使用文档

**验收标准**:
- [ ] 所有测试用例通过
- [ ] 文档完整清晰
- [ ] 无已知bug

---

## Parallel Opportunities

1. **Phase 2 可部分并行**:
   - MemberLevelService 和 MemberService 可并行开发

2. **Phase 4 和 Phase 5 可并行**:
   - API层和页面组件可并行开发

3. **Phase 7 和 Phase 8 可并行**:
   - 累计消费逻辑和订单显示可并行

---

## Success Criteria

### MVP (最小可行产品)

Phase 1-6完成，实现：
- ✅ 会员信息CRUD
- ✅ 会员等级配置
- ✅ 结账时选择会员并应用折扣
- ✅ 前端会员管理页面

### 完整功能

所有Phase完成，额外实现：
- ✅ 订单完成后自动更新累计消费
- ✅ 会员等级自动升级
- ✅ 订单列表显示会员信息
- ✅ 权限控制

---

## Risk Mitigation

### 技术风险

1. **并发更新累计消费**
   - 缓解：使用@Transactional和数据库锁
   - 验证：并发测试

2. **折扣计算精度**
   - 缓解：使用BigDecimal，固定精度
   - 验证：边界值测试

3. **外键约束**
   - 缓解：注意删除顺序
   - 验证：测试删除场景

### 业务风险

1. **会员等级配置错误**
   - 缓解：提供初始化脚本
   - 验证：数据验证规则

2. **累计消费计算错误**
   - 缓解：只在completed状态更新
   - 验证：状态验证

---

## Branch Strategy

建议创建功能分支：
```bash
git checkout -b feature/member-management
```

完成后合并到dev分支：
```bash
git checkout dev
git merge feature/member-management
```

---

## Generated Artifacts

- ✅ Design Spec: `docs/superpowers/specs/2026-03-31-member-management-design.md`
- ✅ Tasks: `tasks.md`
- ✅ Implementation Plan: `plan.md` (当前文件)

**Next Steps**: 开始执行 tasks.md 中的任务，从 Phase 1 开始。
