package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import ru.gnaizel.model.Group;
import ru.gnaizel.repository.GroupRepository;
import ru.gnaizel.telegram.TelegramBot;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GroupInviteHandler {
    private final GroupRepository repository;

    public void groupInviteHandle(Update update, TelegramBot bot) {
        if (!update.hasMyChatMember()) {
            return;
        }

        ChatMemberUpdated chatMemberUpdate = update.getMyChatMember();
        Chat chat = chatMemberUpdate.getChat();
        ChatMember oldChatMember = chatMemberUpdate.getOldChatMember();
        ChatMember newChatMember = chatMemberUpdate.getNewChatMember();
        long chatId = chat.getId();
        String groupName = chat.getTitle();

        if (chat.getType().equals("group") || chat.getType().equals("supergroup")) {

            if (oldChatMember instanceof ChatMemberLeft && newChatMember instanceof ChatMemberMember) {
                String messageText = "Привет! Спасибо, что добавили меня в группу " + groupName + " ! \n"
                        + "Для корректной работы боту нужны права администратора";

                Optional<Group> existingGroup = repository.findByGroupId(chatId);
                if (existingGroup.isPresent()) {
                    Group group = existingGroup.get();
                    group.setGroupTitle(groupName);
                    group.setInviteLink("bot can't copy link");
                    group.setNumberOfMember(1);
                    repository.save(group);
                } else {
                    // Создаем новую запись
                    repository.save(Group.builder()
                            .chatId(chatId)
                            .groupId(chatId)
                            .groupTitle(groupName)
                            .inviteLink("bot can't copy link")
                            .numberOfMember(1)
                            .build());
                }

                bot.sendMessage(chatId, messageText);
            } else if (newChatMember instanceof ChatMemberAdministrator) {
                // Ищем группу по chatId ИЛИ groupId для обработки смены ID
                Optional<Group> groupOpt = repository.findByChatId(chatId)
                        .or(() -> repository.findByGroupId(chatId));

                if (groupOpt.isPresent()) {
                    Group group = groupOpt.get();
                    if (group.getGroupId() != chatId) {
                        group.setGroupId(chatId);
                    }
                    group.setInviteLink(chat.getInviteLink() == null ? "" : chat.getInviteLink());
                    group.setGroupTitle(groupName);
                    repository.save(group);

                    bot.sendMessage(chatId, "Спасибо за админку! Теперь я могу полноценно работать.");
                } else {
                    repository.save(Group.builder()
                            .chatId(chatId)
                            .groupId(chatId)
                            .groupTitle(groupName)
                            .inviteLink(chat.getInviteLink() == null ? "" : chat.getInviteLink())
                            .numberOfMember(1)
                            .build());

                    bot.sendMessage(chatId, "Спасибо за админку! Группа добавлена в базу.");
                }
            } else if (newChatMember instanceof ChatMemberLeft) {
                System.out.println("Меня удалили из чата " + chat.getTitle());
                repository.deleteByChatId(chatId);
            }
        }
    }
}
