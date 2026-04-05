package com.pindou.timer.task;

import com.pindou.timer.dto.RemindInfo;
import com.pindou.timer.service.RemindService;
import com.pindou.timer.websocket.WebsocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 提醒定时任务
 *
 * @author wuci
 * @date 2026-03-28
 */
@Component
public class RemindTask {

    private static final Logger log = LoggerFactory.getLogger(RemindTask.class);

    @Resource
    private RemindService remindService;

    @Resource
    private WebsocketService websocketService;

    /**
     * 每5秒检查一次提醒状态
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkReminders() {
        try {
            List<RemindInfo> reminders = remindService.checkReminders();

            if (!reminders.isEmpty()) {
                // 发送提醒通知
                sendRemindNotifications(reminders);
            }
        } catch (Exception e) {
            log.error("检查提醒状态失败", e);
        }
    }

    /**
     * 发送提醒通知
     * 通过日志记录 + WebSocket推送到前端
     */
    private void sendRemindNotifications(List<RemindInfo> reminders) {
        for (RemindInfo remind : reminders) {
            log.info("提醒通知: 桌台[{}] {} - {}",
                    remind.getTableName(),
                    remind.getRemindTypeDesc(),
                    formatDuration(remind.getUsedDuration()));

            // WebSocket 推送
            websocketService.broadcastRemind(remind);
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d小时%d分", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分", minutes);
        } else {
            return String.format("%d秒", secs);
        }
    }
}
