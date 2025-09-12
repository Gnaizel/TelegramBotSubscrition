package ru.gnaizel.dto.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
}
