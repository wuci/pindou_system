package com.pindou.timer.common.result;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author pindou
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    // ========== 通用错误码 1xxx ==========
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或认证失败"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    SERVER_ERROR(500, "服务器内部错误"),
    INVALID_PARAM(4001, "参数无效"),

    // ========== 用户相关错误码 10xxx ==========
    USER_NOT_FOUND(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已被禁用"),
    USERNAME_EXISTS(10003, "用户名已存在"),
    PASSWORD_ERROR(10004, "密码错误"),
    OLD_PASSWORD_ERROR(10005, "原密码错误"),

    // ========== 角色相关错误码 11xxx ==========
    ROLE_NOT_FOUND(11001, "角色不存在"),
    ROLE_NAME_EXISTS(11002, "角色名称已存在"),
    ROLE_SYSTEM_BUILTIN(11003, "系统内置角色不能删除"),
    ROLE_IN_USE(11004, "角色正在使用中，无法删除"),

    // ========== 桌台相关错误码 20xxx ==========
    TABLE_NOT_FOUND(20001, "桌台不存在"),
    TABLE_STATUS_ERROR(20002, "桌台状态错误"),
    TABLE_HAS_USING(20003, "桌台正在使用中"),
    TABLE_COUNT_INVALID(20004, "桌台数量配置无效"),
    TABLE_COUNT_EXCEED(20005, "桌台数量超过限制"),

    // ========== 订单相关错误码 30xxx ==========
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_STATUS_ERROR(30002, "订单状态错误"),
    ORDER_ALREADY_COMPLETED(30003, "订单已完成"),

    // ========== 配置相关错误码 40xxx ==========
    CONFIG_NOT_FOUND(40001, "配置不存在"),
    CONFIG_UPDATE_FAILED(40002, "配置更新失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
