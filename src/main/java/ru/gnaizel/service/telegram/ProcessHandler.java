package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.AlertTepe;
import ru.gnaizel.exception.MessageValidationError;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.telegram.group.GroupService;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessHandler {

    public static final HashMap<Long, String> inProgress = new HashMap<>();
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final GroupService groupService;

    public boolean checkAndHandle(Update update, TelegramBot bot) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else {
            return false;
        }

        if (!inProgress.containsKey(chatId)) {
            return false;
        }

        if (inProgress.get(chatId).startsWith("setGroupButtonForAlert")) {
            log.debug(inProgress.get(chatId));
            UserDto user;
            if (update.hasMessage()) {
                user = userService.findUserByChatId(update.getMessage().getFrom().getId());
            } else if (update.hasCallbackQuery()) {
                user = userService.findUserByChatId(update.getCallbackQuery().getFrom().getId());
            } else {
                throw new MessageValidationError("Message is not valid (It's not a text, it's not a button)");
            }
            String[] groupIdArray = inProgress.get(chatId).split("-");

            long groupId = Long.parseLong(groupIdArray[1]) * -1;
            AlertTepe tepe = AlertTepe.valueOf(groupIdArray[2]);
            String message = update.getMessage().getText();

            log.debug("Alert: groupId: {} UserId: {} Message: {}", groupId, user.getUserId(), message);

            if (tepe == AlertTepe.GROUP) {
                groupService.sendAlertToGroup(message, groupId, user.getUserId(), bot);
            } else if (tepe == AlertTepe.GROUP_MAMERS) {
                groupService.sendAlertToUser(message, groupId, user.getUserId(), bot);
            }

            inProgress.remove(chatId);

            return true;
        }

        switch (inProgress.get(chatId)) {
            case "schedule_for_teacher":
                String sename = update.getMessage().getText().trim();
                sename = sename.substring(0, 1).toUpperCase() + sename.substring(1).toLowerCase();
                try {
                    bot.sendMessage(MessageFactory.simple(update,
                            scheduleService.fetchAndExtractTeachersSchedule(sename)));
                } catch (ScheduleValidationError e) {
                    bot.sendMessage(MessageFactory.simple(update, "Учитель не найден"));
                }
                inProgress.remove(chatId);
                return true;

            case "setGroup", "editGroup":
                String text = update.getMessage().getText();
                try {
                    if (text == null || text.isEmpty()) {
                        bot.sendMessage(MessageFactory.simple(update, "Поле группы не может быть пустым"));
                        throw new MessageValidationError("message text is empty");
                    }
                    if (text.length() < 3 || text.length() > 10 || !text.contains("-")) {
                        bot.sendMessage(MessageFactory.simple(update, "Это не похоже на название группы"));
                        throw new MessageValidationError("Group validation error");
                    }

                    userService.setCohort(chatId, text.toUpperCase().replaceAll(" ", ""));
                    bot.sendMessage(MessageFactory.simple(update,
                            "Группа изменена на: " + text.toUpperCase()));
                    inProgress.remove(chatId);
                    return true;
                } catch (Exception e) {
                    log.debug("Error setting cohort", e);
                    return true;
                }
            default:
                return false;
        }
    }
}

