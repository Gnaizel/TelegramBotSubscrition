package ru.gnaizel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gnaizel.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    @NotNull
    private Long chatId;
    @NotNull
    private Long userId;
    @NotBlank
    private String userName;
    @NotNull
    private UserStatus userStatus;
    @NotNull
    private byte alertLevel;
    @NotNull
    private LocalDateTime localDateTime;
    private String cohort;
}
