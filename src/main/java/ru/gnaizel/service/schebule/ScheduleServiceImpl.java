package ru.gnaizel.service.schebule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import ru.gnaizel.client.ppk.PpkClient;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.formater.ScheduleFormatter;
import ru.gnaizel.model.ScheduleEntry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final PpkClient ppkClient;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private String html;

    @PostConstruct
    private void init() {
        refreshHtml();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    private void refreshHtml() {
        html = ppkClient.getHtmlScheduleForPpkSite();
        log.info("HTML content refreshed");
    }

    @Override
    public String fetchAndExtractTeachersSchedule(String teachersName) {
        String studentsBlock = ScheduleHtmlParser.extractTeacherBlock(html);
        List<ScheduleEntry> allEntries = ScheduleHtmlParser.parseSchedule(studentsBlock, teachersName)
                .stream().toList();


        return ScheduleFormatter.format(teachersName, allEntries);
    }

    @Override
    public String buildScheduleByDate(String groupName, String korpusName, LocalDate date) {
        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(date);
                })
                .toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }

    @Override
    public String buildScheduleToday(String groupName, String korpusName) {
        LocalDate todayDaty = LocalDate.now();
        
        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(todayDaty);
                }).toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }

    @Override
    public String buildScheduleToNextDay(String groupName, String korpusName) {
        LocalDate nextDay = LocalDate.now().plusDays(1);
        
        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(nextDay);
                })
                .toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }
}
