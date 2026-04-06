package com.pindou.timer.constants;

/**
 * 规则类型枚举
 *
 * @author wuci
 * @date 2026-04-06
 */
public enum RuleTypeEnum {

    /**
     * 表格数据
     */
    TABLE("table", "表格数据"),

    /**
     * 列表
     */
    LIST("list", "列表"),

    /**
     * 警告框
     */
    WARNING("warning", "警告框"),

    /**
     * 特色服务
     */
    SPECIAL("special", "特色服务");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    RuleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 枚举值
     */
    public static RuleTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RuleTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 校验类型代码是否合法
     *
     * @param code 类型代码
     * @return 是否合法
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
