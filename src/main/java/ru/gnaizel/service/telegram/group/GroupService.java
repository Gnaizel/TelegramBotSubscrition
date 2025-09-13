package ru.gnaizel.service.telegram.group;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.telegram.TelegramBot;

public interface GroupService {
    void setGroupModerator(long chatId, long userId, TelegramBot bot);

    void getModeratorApplication(UserDto user, long chatId, TelegramBot bot);

    void vote(CallbackQuery query, TelegramBot bot);

    void devote(CallbackQuery query, TelegramBot bot);
}
