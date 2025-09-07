package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final UserService userService;
    private final ScheduleService scheduleService;
    private final ProfileService profileService;

    public void handle(Update update, TelegramBot bot) {
        String command;

        if (update.getMessage().getText().isBlank()) {
            throw new MessageValidationError("Message can't be blank");
        } else command = update.getMessage().getText();

        if (command.startsWith("/")) {
            command = command.toLowerCase().replace("/", "");
        }

        if (command.contains("@")) {
            String[] commandSplit = command.split("@");
            if (!commandSplit[1].equalsIgnoreCase(bot.getBotUsername())) {
                return;
            }
            command = commandSplit[0];
        }

        log.info("Command received: {}", command);
        UserDto user = userService.findUserByChatId(update.getMessage().getChatId());

        try {
            switch (command) {
                case "profile", "Профиль 🙎‍♂️":
                    profileService.getProfile(update, bot);
                    break;

                case "schedule", "Расписание 📅":
                case "schedule_to_day":
                    String todaySchedule = scheduleService.buildScheduleToday(user.getCohort(), user.getKorpus());
                    bot.sendMessage(MessageFactory.simple(update, todaySchedule));
                    break;

                case "schedule_to_next_day":
                    String nextDaySchedule = scheduleService.buildScheduleToNextDay(user.getCohort(), user.getKorpus());
                    bot.sendMessage(MessageFactory.simple(update, nextDaySchedule));
                    break;

                case "schedule_for_teacher", "Расписание преподавателей 📅":
                    String teacherSchedule = scheduleService.fetchAndExtractTeachersSchedule(user.getCohort());
                    bot.sendMessage(MessageFactory.simple(update, teacherSchedule));
                    break;

                case "start":
                    profileService.startCommand(update, bot);
                    break;

                default:
                    bot.sendMessage(MessageFactory.simple(update, "Эта команда не поддерживается"));
            }
        } catch (ScheduleValidationError e) {
            bot.sendMessage(MessageFactory.simple(update, "Актуального расписания не найдено"));
        }
    }
}
