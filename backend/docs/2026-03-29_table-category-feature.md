# 2026-03-29 桌台分类功能

## 功能概述

为桌台管理页面新增自定义分类功能，支持按分类查看桌台，并保留原有的网格视图和列表视图切换功能。

## 后端修改

### 1. 新增实体
- **TableCategory.java**: 桌台分类实体
  - 字段：id, name, icon, sortOrder, remark, createdAt, updatedAt

### 2. 实体修改
- **Table.java**: 添加 `categoryId` 字段

### 3. 新增 Mapper
- **TableCategoryMapper.java**: 桌台分类 Mapper

### 4. 新增 DTO
- **TableCategoryRequest.java**: 分类请求 DTO
- **TableCategoryResponse.java**: 分类响应 DTO（包含桌台统计）

### 5. 新增 Service
- **TableCategoryService.java**: 分类服务接口
  - `getAllCategories()`: 获取所有分类（含统计）
  - `createCategory()`: 创建分类
  - `updateCategory()`: 更新分类
  - `deleteCategory()`: 删除分类
  - `getCategory()`: 获取分类详情

- **TableCategoryServiceImpl.java**: 分类服务实现

### 6. 新增 Controller
- **TableCategoryController.java**: 分类控制器
  - `GET /table-category/list`: 获取分类列表
  - `POST /table-category/create`: 创建分类
  - `POST /table-category/update`: 更新分类
  - `DELETE /table-category/delete/{id}`: 删除分类
  - `GET /table-category/{id}`: 获取分类详情

### 7. 修改现有接口
- **TableService**: `getTableList()` 添加 `categoryId` 参数
- **TableController**: `GET /tables` 添加 `categoryId` 查询参数

### 8. 数据库迁移
- **add_table_category.sql**: 创建分类表和修改桌台表
  - 创建 `biz_table_category` 表
  - 修改 `biz_table` 表添加 `category_id` 字段
  - 插入默认分类数据（大厅、包间、VIP区）

## 前端修改

### 1. 新增 API
- **tableCategory.ts**: 分类相关 API
  - `getCategories()`: 获取分类列表
  - `createCategory()`: 创建分类
  - `updateCategory()`: 更新分类
  - `deleteCategory()`: 删除分类
  - `getCategory()`: 获取分类详情

### 2. 修改 API
- **table.ts**:
  - `TableInfo` 接口添加 `categoryId` 字段
  - `getTableList()` 添加 `categoryId` 参数

### 3. 重构桌台管理页面
- **TableManagement/index.vue**: 完全重构

#### 新增功能：
- **分类标签栏**: 顶部显示所有分类，支持切换
  - 显示分类名称、桌台数量、使用中数量
  - 点击切换分类筛选
  - 当前选中分类高亮显示

- **视图切换**: 网格视图 / 列表视图
  - 网格视图：卡片式布局，响应式排列
  - 列表视图：表格形式，显示详细信息

- **状态筛选**: 支持按状态筛选（空闲/使用中/暂停）

- **分类管理按钮**: 打开分类管理对话框

### 4. 新增组件
- **CategoryDialog.vue**: 分类管理对话框
  - 显示所有分类列表
  - 新增/编辑/删除分类
  - 表单验证
  - 图标选择

## 功能特点

### 分类管理
1. 默认分类"全部"不可编辑删除
2. 可自定义分类名称、图标、排序
3. 删除分类前检查是否有桌台使用
4. 分类显示桌台总数和使用中数量

### 视图展示
1. **网格视图**:
   - 响应式布局，自适应屏幕
   - 卡片显示桌台状态、时长、金额
   - 不同状态不同颜色边框
   - 悬停动画效果

2. **列表视图**:
   - 表格形式展示详细信息
   - 支持点击行查看详情
   - 快捷操作按钮

### 实时刷新
- 每5秒自动刷新桌台状态和分类统计
- 支持手动刷新

## 使用说明

### 首次使用
1. 执行数据库迁移脚本 `add_table_category.sql`
2. 系统自动创建3个默认分类：大厅、包间、VIP区
3. 现有桌台默认分配到"大厅"分类

### 分类管理
1. 点击"分类管理"按钮
2. 可新增、编辑、删除分类
3. 支持设置图标和排序
4. 删除分类前需确保该分类下无桌台

### 桌台查看
1. 点击顶部分类标签切换查看不同分类的桌台
2. 使用状态筛选器过滤桌台
3. 切换网格/列表视图查看
4. 点击桌台卡片或行查看详情并操作
