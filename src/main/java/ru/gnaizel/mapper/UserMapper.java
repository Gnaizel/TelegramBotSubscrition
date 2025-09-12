package ru.gnaizel.mapper;

import ru.gnaizel.dto.user.UserCreateDto;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.model.User;

public class UserMapper {
    public static User userFromUserCreateDto(UserCreateDto userCreateDto) {
        return User.builder()
                .chatId(userCreateDto.getChatId())
                .userId(userCreateDto.getUserId())
                .userName(userCreateDto.getUserName())
                .cohort("no cohort")
                .korpus("no korpus")
                .alertLevel(userCreateDto.getAlertLevel())
                .userStatus(userCreateDto.getUserStatus())
                .registrationDate(userCreateDto.getLocalDateTime())
                .build();
    }

    public static UserDto userToDto(User user) {
        return  UserDto.builder()
                .chatId(user.getChatId())
                .userId(user.getUserId())
                .userName(user.getUserName())
                .cohort(user.getCohort())
                .korpus(user.getKorpus())
                .alertLevel(user.getAlertLevel())
                .userStatus(user.getUserStatus())
                .registrationDate(user.getRegistrationDate())
                .build();
    }
}
