package com.pindou.timer.event;

import org.springframework.context.ApplicationEvent;

/**
 * 桌台状态变更事件
 *
 * @author wuci
 * @date 2026-03-28
 */
public class TableStatusChangeEvent extends ApplicationEvent {

    private final Integer tableId;

    public TableStatusChangeEvent(Object source, Integer tableId) {
        super(source);
        this.tableId = tableId;
    }

    public Integer getTableId() {
        return tableId;
    }
}
