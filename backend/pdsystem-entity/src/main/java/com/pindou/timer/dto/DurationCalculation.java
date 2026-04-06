package com.pindou.timer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时长计算结果值对象
 *
 * @author wuci
 * @date 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DurationCalculation {

    /** 总时长（秒） */
    private int totalDuration;

    /** 暂停时长（秒） */
    private int pauseDuration;

    /** 实际使用时长（秒） */
    private int actualDuration;

    /**
     * 创建时长计算结果
     *
     * @param totalDuration 总时长
     * @param pauseDuration 暂停时长
     * @return 时长计算结果
     */
    public static DurationCalculation of(int totalDuration, int pauseDuration) {
        int actualDuration = totalDuration - pauseDuration;
        return new DurationCalculation(totalDuration, pauseDuration, actualDuration);
    }

    /**
     * 获取实际使用时长（分钟）
     */
    public int getActualDurationInMinutes() {
        return actualDuration / 60;
    }

    /**
     * 获取总时长（分钟）
     */
    public int getTotalDurationInMinutes() {
        return totalDuration / 60;
    }

    /**
     * 获取暂停时长（分钟）
     */
    public int getPauseDurationInMinutes() {
        return pauseDuration / 60;
    }
}
