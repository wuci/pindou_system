# 2026-04-03 自定义异常类 PDException

## 修改日期
2026-04-03

## 修改内容

### 新增文件

#### 1. PDException 自定义异常类
**文件**：`backend/pdsystem-entity/src/main/java/com/pindou/timer/common/exception/PDException.java`

**说明**：继承自 BusinessException，提供丰富的静态工厂方法用于快速创建常见业务异常

**异常分类**：

| 分类 | 静态方法 | 说明 |
|------|----------|------|
| 权限相关 | `permissionNotFound()` | 权限不存在 |
| 权限相关 | `permissionKeyExists(key)` | 权限编码已存在 |
| 权限相关 | `builtInPermissionNotModifiable()` | 内置权限不允许修改 |
| 权限相关 | `builtInPermissionNotDeletable()` | 内置权限不允许删除 |
| 权限相关 | `hasChildPermissions()` | 存在子权限，不允许删除 |
| 角色相关 | `roleNotFound()` | 角色不存在 |
| 用户相关 | `userNotFound()` | 用户不存在 |
| 通用 | `invalidParam(message)` | 参数校验失败 |
| 通用 | `notFound(resource)` | 资源不存在 |
| 通用 | `conflict(message)` | 资源冲突 |

**使用方式**：

```java
// 方式1：使用静态工厂方法（推荐）
throw PDException.permissionNotFound();
throw PDException.permissionKeyExists("table:view");
throw PDException.builtInPermissionNotModifiable();

// 方式2：使用 ErrorCode 枚举
throw new PDException(ErrorCode.PERMISSION_NOT_FOUND);
throw new PDException(ErrorCode.PERMISSION_KEY_EXISTS, "权限编码已存在");

// 方式3：直接指定错误码和消息
throw new PDException(500, "自定义错误消息");
throw new PDException("自定义错误消息"); // 默认 500 错误码
```

### 修改的文件

#### 1. ErrorCode 枚举
**文件**：`backend/pdsystem-entity/src/main/java/com/pindou/timer/common/result/ErrorCode.java`

**修改内容**：添加权限相关错误码

```java
// ========== 权限相关错误码 60xxx ==========
PERMISSION_NOT_FOUND(60001, "权限不存在"),
PERMISSION_KEY_EXISTS(60002, "权限编码已存在"),
PERMISSION_BUILTIN_NOT_MODIFIABLE(60003, "内置权限不允许修改"),
PERMISSION_BUILTIN_NOT_DELETABLE(60004, "内置权限不允许删除"),
PERMISSION_HAS_CHILDREN(60005, "存在子权限，不允许删除");
```

#### 2. PermissionServiceImpl
**文件**：`backend/pdsystem-service/src/main/java/com/pindou/timer/service/impl/PermissionServiceImpl.java`

**修改内容**：
- 更新导入路径：`com.pindou.timer.exception.PDException` → `com.pindou.timer.common.exception.PDException`
- 使用静态工厂方法替代直接构造

**修改前**：
```java
throw new PDException("权限不存在");
throw new PDException("权限编码已存在");
throw new PDException("内置权限不允许修改");
throw new PDException("内置权限不允许删除");
throw new PDException("存在子权限，不允许删除");
```

**修改后**：
```java
throw PDException.permissionNotFound();
throw PDException.permissionKeyExists(request.getPermissionKey());
throw PDException.builtInPermissionNotModifiable();
throw PDException.builtInPermissionNotDeletable();
throw PDException.hasChildPermissions();
```

## 设计优势

### 1. 类型安全
使用静态工厂方法，可以在编译期发现错误，避免硬编码字符串错误

### 2. 集中管理
所有异常消息和错误码在 ErrorCode 枚举中统一管理

### 3. 可扩展性
新增异常类型只需在 PDException 中添加静态工厂方法

### 4. 代码简洁
调用方代码更简洁易读

### 5. 国际化友好
基于 ErrorCode 的设计便于后续扩展国际化支持

## 错误码规范

遵循项目已有的错误码规范：

| 错误码范围 | 分类 | 说明 |
|-----------|------|------|
| 1xxx | 通用错误码 | 通用系统级错误 |
| 10xxx | 用户相关 | 用户登录、注册等 |
| 11xxx | 角色相关 | 角色管理 |
| 20xxx | 桌台相关 | 桌台操作 |
| 30xxx | 订单相关 | 订单管理 |
| 40xxx | 配置相关 | 系统配置 |
| 50xxx | 会员相关 | 会员管理 |
| 51xxx | 会员等级相关 | 会员等级管理 |
| 60xxx | 权限相关 | 权限管理（新增） |

## 使用示例

### Service 层
```java
@Service
public class SomeServiceImpl implements SomeService {

    public void doSomething(String id) {
        // 查询资源
        Entity entity = mapper.selectById(id);
        if (entity == null) {
            throw PDException.notFound("资源");
        }

        // 检查权限
        if (!hasPermission()) {
            throw new PDException(ErrorCode.FORBIDDEN);
        }

        // 业务校验
        if (entity.getStatus() == INVALID) {
            throw PDException.invalidParam("状态无效");
        }
    }
}
```

### Controller 层
```java
@RestController
public class SomeController {

    @GetMapping("/{id}")
    public Result<Entity> get(@PathVariable String id) {
        try {
            Entity entity = service.getById(id);
            return Result.success(entity);
        } catch (PDException e) {
            // GlobalExceptionHandler 会自动捕获并处理
            throw e;
        }
    }
}
```

## 全局异常处理

项目已配置 GlobalExceptionHandler，会自动捕获并处理所有异常：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }
}
```

## 注意事项

1. **异常继承关系**：PDException → BusinessException → RuntimeException
2. **性能优化**：BusinessException 重写了 `fillInStackTrace()` 方法，不生成堆栈跟踪，提高性能
3. **使用场景**：仅用于业务异常，系统异常应使用标准异常类
4. **错误码唯一性**：添加新错误码时注意不要与现有错误码冲突

## 相关文档
- 无
