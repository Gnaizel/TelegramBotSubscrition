package ru.gnaizel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gnaizel.enums.Subscriptions;
import ru.gnaizel.model.User;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByChatId(Long chatId);

    Optional<User> findByChatId(Long chatId);

    boolean existsByUserId(Long userId);

    Set<User> findDistinctBySubscriptionsContaining(Subscriptions subscription);

    Optional<User> findByUserId(Long userId);
}
