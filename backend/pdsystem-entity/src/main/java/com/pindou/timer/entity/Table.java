package com.pindou.timer.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 桌台实体类
 *
 * @author pindou
 * @since 1.0.0
 */
@Data
@TableName("biz_table")
@Schema(description = "桌台")
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "桌台编号")
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @Schema(description = "桌台名称")
    @TableField("name")
    private String name;

    @Schema(description = "分类ID")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "状态：idle-空闲 using-使用中 paused-暂停")
    @TableField("status")
    private String status;

    @Schema(description = "当前订单ID")
    @TableField("current_order_id")
    private String currentOrderId;

    @Schema(description = "开始时间（毫秒时间戳）")
    @TableField("start_time")
    private Long startTime;

    @Schema(description = "预设时长（秒），null表示不设时长")
    @TableField("preset_duration")
    private Integer presetDuration;

    @Schema(description = "累计暂停时长（秒）")
    @TableField("pause_accumulated")
    private Integer pauseAccumulated;

    @Schema(description = "最后暂停时间（毫秒时间戳）")
    @TableField("last_pause_time")
    private Long lastPauseTime;

    @Schema(description = "是否已提醒：1-是 0-否")
    @TableField("reminded")
    private Integer reminded;

    @Schema(description = "提醒是否被忽略：1-是 0-否")
    @TableField("remind_ignored")
    private Integer remindIgnored;

    @Schema(description = "预定状态：none-未预定，reserved-已预定")
    @TableField("reservation_status")
    private String reservationStatus;

    @Schema(description = "预定截止时间（毫秒时间戳）")
    @TableField("reservation_end_time")
    private Long reservationEndTime;

    @Schema(description = "预订人姓名")
    @TableField("reservation_name")
    private String reservationName;

    @Schema(description = "预订人手机号")
    @TableField("reservation_phone")
    private String reservationPhone;

    @Schema(description = "创建时间（毫秒时间戳）")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    @Schema(description = "更新时间（毫秒时间戳）")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
