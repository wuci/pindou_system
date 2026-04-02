# 会员管理 API 契约

## Base URL

```
http://localhost:8080/api
```

## 1. 会员管理 API

### 1.1 创建会员

**请求**
```http
POST /api/members
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "张三",
  "phone": "13800138000",
  "address": "北京市朝阳区"  // 可选
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "address": "北京市朝阳区",
    "totalAmount": 0.00,
    "levelId": 1,
    "levelName": "豆豆萌新",
    "discountRate": 0.950,
    "createdAt": 1730374800000,
    "updatedAt": 1730374800000
  }
}
```

**错误响应**
```json
{
  "code": 1001,
  "message": "手机号已存在",
  "data": null
}
```

### 1.2 会员列表（分页）

**请求**
```http
GET /api/members?page=1&pageSize=10&keyword=138
Authorization: Bearer {token}
```

**查询参数**
- `page`: 页码（从1开始）
- `pageSize`: 每页大小
- `keyword`: 搜索关键词（手机号或姓名，可选）

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138000",
        "address": "北京市朝阳区",
        "totalAmount": 500.00,
        "levelId": 2,
        "levelName": "熨烫能手",
        "discountRate": 0.900,
        "createdAt": 1730374800000,
        "updatedAt": 1730374800000
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

### 1.3 会员详情

**请求**
```http
GET /api/members/1
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "address": "北京市朝阳区",
    "totalAmount": 500.00,
    "levelId": 2,
    "levelName": "熨烫能手",
    "discountRate": 0.900,
    "createdAt": 1730374800000,
    "updatedAt": 1730374800000
  }
}
```

### 1.4 更新会员

**请求**
```http
PUT /api/members/1
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "张三三",
  "address": "上海市浦东新区"  // 可选
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 1.5 删除会员

**请求**
```http
DELETE /api/members/1
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 1.6 搜索会员

**请求**
```http
GET /api/members/search?keyword=138
Authorization: Bearer {token}
```

**查询参数**
- `keyword`: 搜索关键词（手机号或姓名）

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "totalAmount": 500.00,
      "levelId": 2,
      "levelName": "熨烫能手",
      "discountRate": 0.900
    }
  ]
}
```

## 2. 会员等级 API

### 2.1 获取所有等级

**请求**
```http
GET /api/member-levels
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "豆豆萌新",
      "minAmount": 0.00,
      "maxAmount": 300.00,
      "discountRate": 0.950,
      "sort": 1
    },
    {
      "id": 2,
      "name": "熨烫能手",
      "minAmount": 300.01,
      "maxAmount": 1000.00,
      "discountRate": 0.900,
      "sort": 2
    },
    {
      "id": 3,
      "name": "像素匠人",
      "minAmount": 1000.01,
      "maxAmount": 3000.00,
      "discountRate": 0.850,
      "sort": 3
    },
    {
      "id": 4,
      "name": "熔豆典藏",
      "minAmount": 3000.01,
      "maxAmount": null,
      "discountRate": 0.800,
      "sort": 4
    }
  ]
}
```

### 2.2 创建等级

**请求**
```http
POST /api/member-levels
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "VIP会员",
  "minAmount": 5000.00,
  "maxAmount": null,
  "discountRate": 0.750,
  "sort": 5
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5,
    "name": "VIP会员",
    "minAmount": 5000.00,
    "maxAmount": null,
    "discountRate": 0.750,
    "sort": 5
  }
}
```

### 2.3 更新等级

**请求**
```http
PUT /api/member-levels/5
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "超级VIP",
  "discountRate": 0.700
}
```

### 2.4 删除等级

**请求**
```http
DELETE /api/member-levels/5
Authorization: Bearer {token}
```

### 2.5 初始化默认等级

**请求**
```http
POST /api/member-levels/init
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "初始化完成，共创建4个默认等级",
  "data": null
}
```

## 3. 会员折扣 API

### 3.1 计算折扣

**请求**
```http
POST /api/members/1/discount
Content-Type: application/json
Authorization: Bearer {token}

{
  "originalAmount": 100.00
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "originalAmount": 100.00,
    "discountRate": 0.900,
    "discountAmount": 10.00,
    "finalAmount": 90.00
  }
}
```

## 4. 订单集成 API

### 4.1 结账（支持会员）

**请求**
```http
POST /api/tables/1/end
Content-Type: application/json
Authorization: Bearer {token}

{
  "memberId": 1  // 可选，选择会员后应用折扣
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 4.2 获取桌台账单（含会员折扣）

**请求**
```http
GET /api/tables/1/bill
Authorization: Bearer {token}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": "xxx",
    "tableId": 1,
    "tableName": "桌台1",
    "startTime": 1730374800000,
    "endTime": null,
    "duration": 3600,
    "pauseDuration": 0,
    "actualDuration": 3600,
    "presetDuration": 3600,
    "operatorName": "admin",
    "status": "active",
    "amountDetail": {
      "normalAmount": 19.00,
      "overtimeAmount": 0.00,
      "totalAmount": 19.00
    },
    "member": {
      "id": 1,
      "name": "张三",
      "levelName": "熨烫能手",
      "discountRate": 0.900,
      "discountAmount": 1.90,
      "finalAmount": 17.10
    }
  }
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 1001 | 手机号已存在 |
| 1002 | 会员不存在 |
| 1003 | 会员等级不存在 |
| 1004 | 参数验证失败 |
| 1005 | 无权限 |
| 500 | 系统错误 |

## 认证方式

所有API请求需要在Header中携带JWT Token：

```
Authorization: Bearer {token}
```

Token通过登录接口获取：
```
POST /api/auth/login
```
