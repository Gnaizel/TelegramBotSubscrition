package ru.gnaizel.service.schebule;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.gnaizel.client.ppk.PpkClient;
import ru.gnaizel.enums.Subscriptions;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.formater.ScheduleFormatter;
import ru.gnaizel.model.Group;
import ru.gnaizel.model.ScheduleEntry;
import ru.gnaizel.model.User;
import ru.gnaizel.repository.GroupRepository;
import ru.gnaizel.repository.UserRepository;
import ru.gnaizel.telegram.TelegramBot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final PpkClient ppkClient;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final ApplicationEventPublisher publisher;

    private String html;

    @Scheduled(cron = "0 0 * * * ?")
    private void refreshHtml() {
//        html = ppkClient.getHtmlScheduleForPpkSite();
//        log.info("HTML content refreshed");

        try {
            String newHtml = ppkClient.getHtmlScheduleForPpkSite();
            if (newHtml != null && !newHtml.isBlank()) {
                if (!newHtml.equals(html)) {
                    html = newHtml;
                    log.info("HTML content refreshed");

                    publisher.publishEvent(new ScheduleUpdatedEvent(true));
                }
            } else {
                log.warn("New HTML is empty, keeping old version");
            }
        } catch (Exception e) {
            log.error("Failed to refresh HTML, keeping old version", e);
        }
    }

    @Override
    public void alertNewScheduleToWeekGroupSub(TelegramBot bot) {
        Set<Group> groups = groupRepository.findDistinctBySubscriptionsContaining(Subscriptions.SCHEDULE_EVERY_WEEK);
        log.debug(groups.toString());

        for (Group group : groups) {
            if (group.getSubscriptions().contains(Subscriptions.SCHEDULE_EVERY_WEEK)) {

                String schedule = buildScheduleToWeek(group.getCohort(), group.getKorpus());
                if (!schedule.isBlank()) {
                    bot.sendMessage(group.getChatId(), "Ежедневное расписание \n" + schedule);
                    log.debug("Отправлено еженедельное расписание Группа: {}", group.getGroupTitle());
                }
            }
        }
    }

    @Override
    public void alertNewScheduleToWeekUserSub(TelegramBot bot) {
        Set<User> users = userRepository.findDistinctBySubscriptionsContaining(Subscriptions.SCHEDULE_EVERY_WEEK);
        log.debug(users.toString());

        for (User user : users) {
            if (user.getSubscriptions().contains(Subscriptions.SCHEDULE_EVERY_WEEK)) {
                long chatId = user.getChatId();

                String schedule = buildScheduleToWeek(user.getCohort(), user.getKorpus());
                if (schedule != null && !schedule.isBlank()) {
                    bot.sendMessage(chatId, schedule);
                }
            }
        }
    }

    @Override
    public void alertNewScheduleTodayGroupSub(TelegramBot bot) {
        Set<Group> groups = groupRepository.findDistinctBySubscriptionsContaining(Subscriptions.SCHEDULE_EVERY_WEEK);
        log.debug(groups.toString());

        for (Group group : groups) {
            if (group.getSubscriptions().contains(Subscriptions.SCHEDULE_EVERY_DAY)) {

                String schedule = buildScheduleToday(group.getCohort(), group.getKorpus());
                if (!schedule.isBlank()) {
                    bot.sendMessage(group.getChatId(), "Ежедневное расписание \n" + schedule);
                    log.debug("Отправлено ежедневное расписание Группа: {}", group.getGroupTitle());
                }
            }
        }
    }

    @Override
    public void alertNewScheduleTodayUserSub(TelegramBot bot) {
        Set<User> users = userRepository
                .findDistinctBySubscriptionsContaining(Subscriptions.SCHEDULE_EVERY_DAY);

        for (User user : users) {
            if (user.getSubscriptions().contains(Subscriptions.SCHEDULE_EVERY_DAY)) {
                long userId = user.getUserId();

                String schedule = buildScheduleToday(user.getCohort(), user.getKorpus());
                if (!schedule.isBlank()) {
                    bot.sendMessage(userId, "Ежедневное расписание \n" + schedule);
                    log.debug("Отправлено ежедневное расписание Пользователь: {}", user.getUserName());
                }
            }
        }

        log.debug(users.toString());
    }

    @PostConstruct
    private void init() {
        refreshHtml();
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
    public String buildScheduleToWeek(String groupName, String korpusName) {
        LocalDate todayDaty = LocalDate.now();

        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream().toList();

        if (!entries.isEmpty()) {
            return ScheduleFormatter.format(groupName, entries);
        }

        throw new ScheduleValidationError("Not found any schedule for " + groupName);
    }

    @Override
    public String buildScheduleToday(String groupName, String korpusName) {
        LocalDate toDay = LocalDate.now();

        String studentsBlock = ScheduleHtmlParser.extractStudentsBlock(html);
        List<ScheduleEntry> entries = ScheduleHtmlParser.parseSchedule(studentsBlock, groupName);

        entries = entries.stream()
                .filter(entri -> {
                    String[] dateParse = entri.getDay().split(" ");
                    return LocalDate.parse(dateParse[1], dateTimeFormatter).equals(toDay);
                })
                .toList();

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
