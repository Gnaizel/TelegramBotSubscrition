package ru.gnaizel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gnaizel.enums.Subscriptions;
import ru.gnaizel.model.Group;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupId(Long groupId);

    Set<Group> findDistinctBySubscriptionsContaining(Subscriptions subscription);

    Optional<Group> findByGroupTitle(String groupTitle);

    Optional<Group> findByChatId(Long chatId);
}
