package ru.gnaizel.dto.user;

import lombok.Builder;
import lombok.Data;
import ru.gnaizel.enums.UserStatus;
import ru.gnaizel.model.Group;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserDto {
    private Long chatId;
    private Long userId;
    private String userName;
    private String korpus;
    private String cohort;
    private LocalDateTime registrationDate;
    private UserStatus userStatus;
    private List<Group> groups;
    private byte alertLevel;
}
