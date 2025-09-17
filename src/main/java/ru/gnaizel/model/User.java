package ru.gnaizel.model;

import jakarta.persistence.*;
import lombok.*;
import ru.gnaizel.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@ToString(exclude = "groups")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private Long userId;
    private String userName;
    private String korpus;
    private String cohort;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;
    private byte alertLevel;

    private LocalDateTime registrationDate;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private List<Group> groups;
}
