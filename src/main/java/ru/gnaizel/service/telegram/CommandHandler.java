package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.model.GroupMessage;
import ru.gnaizel.repository.GroupMessageRepository;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {
    private final GroupMessageRepository messageRepository;
    private final UserService userService;
    private final ScheduleService scheduleService;
    private final ProfileService profileService;
    private final MenuService menuService;

    public void handle(Update update, TelegramBot bot) {
        String command;

        if (update.getMessage().getText().isBlank()) {
            throw new MessageValidationError("Message can't be blank");
        } else command = update.getMessage().getText();

        if (command.startsWith("/")) {
            command = command.toLowerCase().replace("/", "");
        }

        if (command.contains("@")) { // Реализация для групп
            String[] commandSplit = command.split("@");
            if (!commandSplit[1].equalsIgnoreCase(bot.getBotUsername())) {
                return;
            }
            command = commandSplit[0];
        }
        userService.checkingForANewUserByMassage(update, bot);
        menuService.createMenuCommand(bot);

        log.info("Command received: {}", command);
        UserDto user = userService.findUserByChatId(update.getMessage().getFrom().getId());

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
                    bot.sendMessage(user.getChatId(), "Введите фамилию преподавателя: ");
                    ProcessHandler.inProgress.put(user.getChatId(), "schedule_for_teacher");
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

    public void handleGroup(Update update, TelegramBot bot) {
        String command;
        Message message = update.getMessage();

        if (update.getMessage().getText().isBlank()) {
            throw new MessageValidationError("Message can't be blank");
        } else command = update.getMessage().getText();

        if (command.startsWith("/")) {
            command = command.toLowerCase().replace("/", "");
        }

        userService.checkingForANewUserByMassage(update, bot);

        UserDto user = userService.findUserByChatId(update.getMessage().getFrom().getId());

        if (command.contains("@")) { // Реализация для групп
            String[] commandSplit = command.split("@");
            if (!commandSplit[1].equalsIgnoreCase(bot.getBotUsername())) {
                return;
            }
            command = commandSplit[0];
        } else {
            messageRepository.save(GroupMessage.builder()
                    .messageText(message.getText())
                    .groupId(message.getChatId())
                    .groupTitle(message.getChat().getTitle())
                    .userId(message.getFrom().getId())
                    .userName(user.getUserName())
                    .userId(update.getMessage().getFrom().getId())
                    .build());
        }

        log.info("Command from group received: {}", command);
    }
}
