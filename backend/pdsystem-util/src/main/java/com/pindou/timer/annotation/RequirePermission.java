package com.pindou.timer.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 用于标注需要特定权限才能访问的方法
 *
 * @author wuci
 * @date 2026-03-28
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限代码
     * 支持多个权限，满足任意一个即可通过
     *
     * @return 权限代码数组
     */
    String[] value();

    /**
     * 权限验证模式
     * AND: 需要拥有所有权限
     * OR: 拥有任意一个权限即可
     *
     * @return 验证模式
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑运算枚举
     */
    enum Logical {
        /**
         * 且：需要拥有所有权限
         */
        AND,

        /**
         * 或：拥有任意一个权限即可
         */
        OR
    }
}
