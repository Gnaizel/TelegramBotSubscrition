package ru.gnaizel.service.schebule;

import java.time.LocalDate;

public interface ScheduleService {
    String buildScheduleByDate(String groupName, String korpusName, LocalDate date);

    String fetchAndExtractTeachersSchedule(String teacherSeName);

    String buildScheduleToday(String groupName, String korpusName);

    String buildScheduleToNextDay(String groupName, String korpusName);

    String buildScheduleToWeek(String groupName, String korpusName);
}
