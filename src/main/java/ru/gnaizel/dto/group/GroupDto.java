package ru.gnaizel.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.gnaizel.enums.Subscriptions;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long id;
    private Long groupId;
    private String groupTitle;
    private String userName;
    private String inviteLink;
    private int numberOfMember;
    private String korpus;
    private String cohort;
    private Subscriptions subscriptions;
}
