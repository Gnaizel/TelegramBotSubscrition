package ru.gnaizel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gnaizel.model.Group;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    void deleteByChatId(Long chatId);

    Optional<Group> findByGroupId(Long groupId);

    Optional<Group> findByChatId(Long chatId);
}
