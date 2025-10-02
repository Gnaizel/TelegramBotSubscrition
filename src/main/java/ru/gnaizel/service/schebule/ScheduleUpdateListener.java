package ru.gnaizel.service.schebule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleUpdateListener {

    private final TelegramBot telegramBot;
    private final ScheduleService scheduleService;

    @EventListener
    public void handleScheduleUpdate(ScheduleUpdatedEvent event) {
        if (event.isHtmlChanged()) {
            log.info("Schedule updated, sending notifications...");

            scheduleService.alertNewScheduleToWeekGroupSub(telegramBot);
//            scheduleService.alertNewScheduleTodayGroupSub(telegramBot);
        }
    }
}
