package com.pindou.timer.constants;

/**
 * 规则分类枚举
 *
 * @author wuci
 * @date 2026-04-06
 */
public enum RuleCategoryEnum {

    /**
     * 套餐规则
     */
    PACKAGES("packages", "套餐规则"),

    /**
     * 增值服务
     */
    SERVICES("services", "增值服务"),

    /**
     * 安全须知
     */
    SAFETY("safety", "安全须知"),

    /**
     * 其他规定
     */
    OTHER("other", "其他规定");

    /**
     * 分类代码
     */
    private final String code;

    /**
     * 分类名称
     */
    private final String name;

    RuleCategoryEnum(String code, String name) {
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
     * @param code 分类代码
     * @return 枚举值
     */
    public static RuleCategoryEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RuleCategoryEnum category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 校验分类代码是否合法
     *
     * @param code 分类代码
     * @return 是否合法
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
