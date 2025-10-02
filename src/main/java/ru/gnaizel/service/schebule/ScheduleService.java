package ru.gnaizel.service.schebule;

import ru.gnaizel.telegram.TelegramBot;

import java.time.LocalDate;

public interface ScheduleService {
    void alertNewScheduleToWeekGroupSub(TelegramBot bot);

    void alertNewScheduleTodayGroupSub(TelegramBot bot);

    void alertNewScheduleToWeekUserSub(TelegramBot bot);

    void alertNewScheduleTodayUserSub(TelegramBot bot);

    String buildScheduleByDate(String groupName, String korpusName, LocalDate date);

    String fetchAndExtractTeachersSchedule(String teacherSeName);

    String buildScheduleToday(String groupName, String korpusName);

    String buildScheduleToNextDay(String groupName, String korpusName);

    String buildScheduleToWeek(String groupName, String korpusName);
}
