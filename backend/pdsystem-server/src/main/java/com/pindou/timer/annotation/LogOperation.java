package com.pindou.timer.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标注需要记录操作日志的方法
 *
 * @author wuci
 * @date 2026-03-28
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作模块
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作类型
     * 如：登录、登出、新增、修改、删除、查询等
     *
     * @return 操作类型
     */
    String operation() default "";

    /**
     * 操作描述
     * 支持SpEL表达式，可以使用 #paramName 获取参数值
     *
     * @return 操作描述
     */
    String description() default "";

    /**
     * 是否保存请求参数
     *
     * @return true-保存，false-不保存
     */
    boolean saveParams() default true;

    /**
     * 是否保存响应结果
     *
     * @return true-保存，false-不保存
     */
    boolean saveResult() default false;
}
