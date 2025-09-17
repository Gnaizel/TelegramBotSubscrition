package ru.gnaizel.service.telegram.group;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.Group;
import ru.gnaizel.telegram.TelegramBot;

import java.util.List;

public interface GroupService {
    void setGroupModerator(long chatId, long userId, TelegramBot bot);

    Group findOfGroupId(long groupId);

    Group findOfGroupChatId(long chatId);

    void getModeratorApplication(UserDto user, long chatId, TelegramBot bot);

    List<Group> getGroupsOfUserElder(long userId);

    List<Group> getGroupsOfUser(long userId);

    void sendAlertGroupMenu(long userId, TelegramBot bot);

    void sendAlert(String message, long groupChatId, long userId, TelegramBot bot);

    void vote(CallbackQuery query, TelegramBot bot);

    void devote(CallbackQuery query, TelegramBot bot);
}
