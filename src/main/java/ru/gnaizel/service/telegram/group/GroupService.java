package ru.gnaizel.service.telegram.group;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.AlertTepe;
import ru.gnaizel.model.Group;
import ru.gnaizel.telegram.TelegramBot;

import java.util.List;

public interface GroupService {
    void setGroupModerator(long chatId, long userId, TelegramBot bot);

    Group findOfGroupChatId(long chatId);

    void getModeratorApplication(UserDto user, long chatId, TelegramBot bot);

    List<Group> getGroupsOfUserElder(long userId);

    List<Group> getGroupsOfUser(long userId);

    void groupSettings(long userId, long groupId, TelegramBot bot);

    void groupAlertSettings(long userId, long groupId, TelegramBot bot);

    void setGroupSubEveryWeekSchedule(long user, long groupId, TelegramBot bot);

    void setGroupSubEveryDaySchedule(long user, long groupId, TelegramBot bot);

    void sendGroupMenuForGroupSettings(long userId, TelegramBot bot);

    void sendAlertGroupMenu(long userId, AlertTepe tepe, TelegramBot bot);

    void sendChoseTepeAlertMenu(long userId, TelegramBot bot);

    void sendAlertToGroup(String message, long groupChatId, long userId, TelegramBot bot);

    void sendAlertToUser(String message, long groupChatId, long userId, TelegramBot bot);

    void vote(CallbackQuery query, TelegramBot bot);

    void devote(CallbackQuery query, TelegramBot bot);
}
