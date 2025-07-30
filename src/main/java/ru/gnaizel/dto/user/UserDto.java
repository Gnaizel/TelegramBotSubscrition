package ru.gnaizel.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private Long chatId;
    private Long userId;
    private String userName;
    private String korpus;
    private String cohort;
    private LocalDateTime registrationDate;
}
