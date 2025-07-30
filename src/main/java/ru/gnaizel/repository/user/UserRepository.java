package ru.gnaizel.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gnaizel.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByChatId(Long chatId);

    Optional<User> findByChatId(Long chatId);

    boolean existsByUserId(Long userId);
}
