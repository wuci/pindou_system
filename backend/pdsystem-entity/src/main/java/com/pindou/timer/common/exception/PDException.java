package com.pindou.timer.common.exception;

import com.pindou.timer.common.result.ErrorCode;

/**
 * 拼豆计时系统业务异常
 * 继承自 BusinessException，提供更丰富的异常构造方法
 *
 * @author wuci
 * @date 2026-04-03
 */
public class PDException extends BusinessException {

    public PDException(String message) {
        super(500, message);
    }

    public PDException(Integer code, String message) {
        super(code, message);
    }

    public PDException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PDException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    // ========== 权限相关异常 ==========

    /**
     * 权限不存在
     */
    public static PDException permissionNotFound() {
        return new PDException(ErrorCode.PERMISSION_NOT_FOUND);
    }

    /**
     * 权限编码已存在
     */
    public static PDException permissionKeyExists(String key) {
        return new PDException(ErrorCode.PERMISSION_KEY_EXISTS, "权限编码[" + key + "]已存在");
    }

    /**
     * 内置权限不允许修改
     */
    public static PDException builtInPermissionNotModifiable() {
        return new PDException(ErrorCode.PERMISSION_BUILTIN_NOT_MODIFIABLE);
    }

    /**
     * 内置权限不允许删除
     */
    public static PDException builtInPermissionNotDeletable() {
        return new PDException(ErrorCode.PERMISSION_BUILTIN_NOT_DELETABLE);
    }

    /**
     * 存在子权限，不允许删除
     */
    public static PDException hasChildPermissions() {
        return new PDException(ErrorCode.PERMISSION_HAS_CHILDREN);
    }

    // ========== 角色相关异常 ==========

    /**
     * 角色不存在
     */
    public static PDException roleNotFound() {
        return new PDException(ErrorCode.ROLE_NOT_FOUND);
    }

    // ========== 用户相关异常 ==========

    /**
     * 用户不存在
     */
    public static PDException userNotFound() {
        return new PDException(ErrorCode.USER_NOT_FOUND);
    }

    // ========== 通用异常 ==========

    /**
     * 参数校验失败
     */
    public static PDException invalidParam(String message) {
        return new PDException(ErrorCode.INVALID_PARAM, message);
    }

    /**
     * 资源不存在
     */
    public static PDException notFound(String resource) {
        return new PDException(ErrorCode.NOT_FOUND, resource + "不存在");
    }

    /**
     * 资源冲突
     */
    public static PDException conflict(String message) {
        return new PDException(ErrorCode.CONFLICT, message);
    }
}
