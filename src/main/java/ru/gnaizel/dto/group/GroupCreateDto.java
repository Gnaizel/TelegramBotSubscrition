package ru.gnaizel.dto.group;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class GroupCreateDto {
    @NotNull
    private Long groupId;
    @Nullable
    private String groupTitle;
    @Nullable
    private String userName;
    @Nullable
    private String inviteLink;
    private int numberOfMember;
}
