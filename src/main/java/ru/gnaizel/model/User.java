package ru.gnaizel.model;

import jakarta.persistence.*;
import lombok.*;
import ru.gnaizel.enums.Subscriptions;
import ru.gnaizel.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    @ElementCollection(targetClass = Subscriptions.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "users_subscriptions_association",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type")
    private Set<Subscriptions> subscriptions;

    private LocalDateTime registrationDate;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private List<Group> groups;
}
