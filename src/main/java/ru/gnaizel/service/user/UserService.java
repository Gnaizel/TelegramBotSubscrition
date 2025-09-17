package ru.gnaizel.service.user;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.Group;
import ru.gnaizel.model.User;
import ru.gnaizel.telegram.TelegramBot;

public interface UserService {
    boolean checkingForANewUserByMassage(Update update, TelegramBot bot);

    void addGroup(long userId, Group group);

    void setAlertLevel(long userId, byte alertLevel);

    UserDto findUserByChatId(long id);

    User createUser(Update update);

    boolean setCohort(Long chatId, String cohort);

    boolean setKorpus(long chatId, String korpus);
}
