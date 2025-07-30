package ru.gnaizel.service.user;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.User;

public interface UserService {
    boolean checkingForANewUserByMassage(Update update);

    UserDto findUserByChatId(long id);

    User createUser(long chatId, long userId, String userName);

    boolean setCohort(Long chatId, String cohort);

    boolean setKorpus(long chatId, String korpus);
}
