# 会员管理功能 - 实现任务清单

**功能**: 会员管理系统
**日期**: 2026-03-31
**技术栈**: Spring Boot + MyBatis Plus (后端) + Vue 3 + TypeScript + Element Plus (前端)

---

## Phase 1: 数据库和基础设置

**目标**: 创建数据库表结构，初始化默认数据

- [ ] T001 创建会员等级配置表 `backend/sql/20260331/add_member_level.sql`
- [ ] T002 创建会员表 `backend/sql/20260331/add_member.sql`
- [ ] T003 修改订单表增加会员字段 `backend/sql/20260331/update_order_member.sql`
- [ ] T004 验证数据库表创建和约束正确性

---

## Phase 2: 后端基础 - 实体类和DTO

**目标**: 创建所有实体类和数据传输对象

- [ ] T005 [P] 创建Member实体类 `backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/Member.java`
- [ ] T006 [P] 创建MemberLevel实体类 `backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/MemberLevel.java`
- [ ] T007 [P] 创建CreateMemberRequest DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/CreateMemberRequest.java`
- [ ] T008 [P] 创建UpdateMemberRequest DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/UpdateMemberRequest.java`
- [ ] T009 [P] 创建MemberResponse DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/MemberResponse.java`
- [ ] T010 [P] 创建CreateMemberLevelRequest DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/CreateMemberLevelRequest.java`
- [ ] T011 [P] 创建UpdateMemberLevelRequest DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/UpdateMemberLevelRequest.java`
- [ ] T012 [P] 创建MemberLevelResponse DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/MemberLevelResponse.java`
- [ ] T013 [P] 创建DiscountDetail DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/DiscountDetail.java`
- [ ] T014 [P] 创建MemberQueryRequest DTO `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/MemberQueryRequest.java`
- [ ] T015 [P] 更新Order实体增加会员字段 `backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/Order.java`
- [ ] T016 [P] 更新OrderInfoResponse增加会员信息 `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/OrderInfoResponse.java`
- [ ] T017 [P] 更新OrderDetailResponse增加会员信息 `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/OrderDetailResponse.java`
- [ ] T018 [P] 更新BillResponse增加会员相关字段 `backend/pdsystem-entity/src/main/java/com/pindou/timer/dto/BillResponse.java`

---

## Phase 3: 用户故事 - US1 会员信息管理

**目标**: 实现会员信息的CRUD功能，支持手机号唯一性校验和默认等级分配

**独立测试标准**:
- 可以创建新会员，默认分配最低等级
- 手机号唯一性校验生效
- 可以查询、编辑、删除会员
- 支持按手机号搜索会员

- [ ] T019 [P] 创建Mapper接口 `backend/pdsystem-mapper/src/main/java/com/pindou/timer/mapper/MemberMapper.java`
- [ ] T020 [P] 创建MemberService接口 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/MemberService.java`
- [ ] T021 实现 MemberService `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/MemberServiceImpl.java`
  - 实现createMember方法（默认分配最低等级）
  - 实现validatePhoneUnique方法
  - 实现calculateLevelId方法
- [ ] T022 创建MemberController `backend/pdsystem-server/src/main/java/com/pindou/timer/controller/MemberController.java`
  - POST /members - 创建会员
  - GET /members - 会员列表（分页、搜索）
  - GET /members/{id} - 会员详情
  - PUT /members/{id} - 更新会员
  - DELETE /members/{id} - 删除会员
  - GET /members/search - 根据手机号搜索会员
- [ ] T023 [US1] 添加会员相关错误码到ErrorCode `backend/pdsystem-common/src/main/java/com/pindou/timer/common/result/ErrorCode.java`

---

## Phase 4: 用户故事 - US2 会员等级配置

**目标**: 实现会员等级的配置管理，支持初始化默认等级数据

**独立测试标准**:
- 可以初始化4个默认等级
- 可以创建、编辑、删除自定义等级
- 折扣率范围验证生效（0.1-1.0）
- 可以根据累计消费金额查询对应等级

- [ ] T024 [P] 创建MemberLevelMapper接口 `backend/pdsystem-mapper/src/main/java/com/pindou/timer/mapper/MemberLevelMapper.java`
- [ ] T025 [P] 创建MemberLevelService接口 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/MemberLevelService.java`
- [ ] T026 实现 MemberLevelService `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/MemberLevelServiceImpl.java`
  - 实现initDefaultLevels方法
  - 实现findLevelByAmount方法
- [ ] T027 创建MemberLevelController `backend/pdsystem-server/src/main/java/com/pindou/timer/controller/MemberLevelController.java`
  - GET /member-levels - 获取所有等级
  - POST /member-levels - 创建等级
  - PUT /member-levels/{id} - 更新等级
  - DELETE /member-levels/{id} - 删除等级
  - POST /member-levels/init - 初始化默认等级
- [ ] T028 [US2] 创建等级初始化SQL脚本 `backend/sql/20260331/init_member_levels.sql`

---

## Phase 5: 用户故事 - US3 会员折扣应用

**目标**: 实现结账时选择会员并应用折扣，改造结账对话框

**独立测试标准**:
- 结账对话框可以选择会员
- 显示会员等级和折扣信息
- 正确计算折扣金额和实付金额
- 订单保存时关联会员ID和原价信息

- [ ] T029 [P] 创建会员API接口 `frontend/src/api/member.ts`
- [ ] T030 [P] 创建会员等级API接口 `frontend/src/api/memberLevel.ts`
- [ ] T031 [P] 在MemberService中实现calculateDiscount方法 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/MemberServiceImpl.java`
- [ ] T032 [US3] 更新endTable方法支持会员ID参数 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`
- [ ] T033 [US3] 更新getBill方法返回会员折扣信息 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`
- [ ] T034 [US3] 创建MemberDialog组件 `frontend/src/components/MemberDialog.vue`
- [ ] T035 [US3] 创建MemberLevelConfig组件 `frontend/src/components/MemberLevelConfig.vue`
- [ ] T036 [US3] 改造BillDialog组件增加会员选择 `frontend/src/components/BillDialog.vue`
  - 添加会员选择下拉框
  - 添加会员折扣信息显示
  - 添加折扣金额计算逻辑
- [ ] T037 [US3] 更新endTable API支持会员参数 `frontend/src/api/table.ts`

---

## Phase 6: 用户故事 - US4 累计消费和等级升级

**目标**: 订单完成后更新会员累计消费，实现等级自动升级

**独立测试标准**:
- 已完成订单正确累加到会员累计消费
- 已作废订单不累加
- 累计消费跨等级时自动升级
- 等级升级记录日志

- [ ] T038 [US4] 在MemberService中实现updateTotalAmount方法 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/MemberServiceImpl.java`
  - 更新累计消费
  - 重新计算等级
  - 处理等级升级
- [ ] T039 [US4] 在endTimer方法中调用会员累计消费更新 `backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/TableServiceImpl.java`
  - 仅在订单状态为completed时更新
  - cancelled状态不更新
- [ ] T040 [US4] 创建会员等级变更日志记录（可选）`backend/pdsystem-entity/src/main/java/com/pindou/timer/entity/MemberLevelLog.java`

---

## Phase 7: 用户故事 - US5 前端管理页面

**目标**: 实现会员管理页面和等级配置页面

**独立测试标准**:
- 会员管理页面显示会员列表
- 支持新增、编辑、删除会员
- 等级配置页面显示等级列表
- 支持新增、编辑、删除等级
- 路由配置正确

- [ ] T041 [P] 创建会员管理页面 `frontend/src/views/MemberManagement/index.vue`
  - 会员列表表格
  - 搜索功能
  - 分页功能
  - 新增/编辑/删除按钮
- [ ] T042 [P] 创建会员等级配置页面 `frontend/src/views/Settings/MemberLevelConfig.vue`
  - 等级列表表格
  - 新增/编辑/删除按钮
  - 初始化默认等级按钮
- [ ] T043 [P] 配置会员管理路由 `frontend/src/router/index.ts`
- [ ] T044 [P] 配置侧边栏菜单 `frontend/src/layout/components/Sidebar.vue`
- [ ] T045 [P] 创建会员API类型定义 `frontend/src/api/member.ts` (如果未在T029创建)
- [ ] T046 [P] 创建会员等级API类型定义 `frontend/src/api/memberLevel.ts` (如果未在T030创建)
- [ ] T047 [US5] 更新订单列表显示会员信息 `frontend/src/views/OrderManagement/index.vue`
- [ ] T048 [US5] 更新订单详情显示会员信息 `frontend/src/views/OrderManagement/Detail.vue`

---

## Phase 8: 支付和用户特权

**目标**: 为会员管理功能添加用户权限控制

- [ ] T049 添加会员管理权限到角色权限配置
- [ ] T050 在sys_role表的permissions字段添加会员相关权限
  - member:view - 查看会员
  - member:create - 创建会员
  - member:edit - 编辑会员
  - member:delete - 删除会员
  - member_level:view - 查看会员等级
  - member_level:manage - 管理会员等级

---

## Phase 9: 集成和验证

**目标**: 端到端测试和集成验证

- [ ] T051 验证会员创建流程（新增会员→默认最低等级）
- [ ] T052 验证会员等级配置（初始化默认等级→自定义等级）
- [ ] T053 验证结账折扣流程（选择会员→显示折扣→计算实付）
- [ ] T054 验证累计消费和升级（完成订单→累计消费→自动升级）
- [ ] T055 验证手机号唯一性校验（重复手机号报错）
- [ ] T056 验证折扣计算精度（边界值测试）
- [ ] T057 验证已作废订单不累加累计消费
- [ ] T058 验证并发场景（同一会员多笔订单同时完成）

---

## Phase 10: 文档和部署

**目标**: 完成文档和准备部署

- [ ] T059 创建会员管理功能使用文档
- [ ] T060 更新API文档
- [ ] T061 准备数据库升级脚本

---

## 依赖关系图

```
Phase 1 (数据库)
  ↓
Phase 2 (实体类和DTO)
  ↓
Phase 3 (US1 - 会员信息管理) ← Phase 4 (US2 - 会员等级配置)
  ↓
Phase 5 (US3 - 折扣应用)
  ↓
Phase 6 (US4 - 累计消费和升级)
  ↓
Phase 7 (US5 - 前端页面)
  ↓
Phase 8 (权限)
  ↓
Phase 9 (集成验证)
  ↓
Phase 10 (文档部署)
```

---

## 并行执行机会

**Phase 2 可以并行执行**（所有实体类和DTO互不依赖）:
- T005, T006, T007, T008, T009, T010, T011, T012, T013, T014, T015, T016, T017, T018

**Phase 3 和 Phase 4 可以并行执行**（会员和等级独立开发）:
- T019-T023 (US1) 与 T024-T028 (US2)

**Phase 7 可以部分并行执行**:
- T041, T043, T044, T045, T047, T048 (前端页面和API）
- T042, T046 (等级配置页面）

---

## MVP 范围建议

**最小可行产品 (MVP)**: Phase 1-5, 7-9
- 包含完整的会员信息管理
- 包含会员等级配置
- 包含结账折扣应用
- 包含前端会员管理页面

**后续迭代**: Phase 6, 10
- 累计消费和等级自动升级
- 完善文档和部署

---

## 实施策略

1. **增量交付**: 按阶段顺序实施，每个阶段完成后可独立测试验证
2. **前后端并行**: Phase 2完成后，前后端可以并行开发（Phase 3-7）
3. **测试驱动**: 每个用户故事完成后立即验证独立测试标准
4. **风险控制**: Phase 1和2是基础，必须确保质量后再进行后续开发
