package ru.gnaizel.service.telegram.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.model.Group;
import ru.gnaizel.repository.GroupRepository;
import ru.gnaizel.telegram.TelegramBot;

import java.util.Optional;

@Slf4j
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
        log.debug("Group chatId: {}, groupName: {}", chatId, groupName);

        if (!(chat.getType().equals("group") || chat.getType().equals("supergroup"))) {
            return;
        }

        Optional<Group> existingGroup = repository.findByGroupTitle(groupName);

        if (oldChatMember instanceof ChatMemberLeft && newChatMember instanceof ChatMemberMember) {
            String messageText;

            if (existingGroup.isPresent()) {
                Group group = existingGroup.get();
                group.setChatId(chatId);
                group.setGroupTitle(groupName);
                group.setInviteLink("bot can't copy link");
                repository.save(group);
                messageText = "";
            } else {
                repository.save(Group.builder()
                        .chatId(chatId)
                        .groupId(chatId)
                        .groupTitle(groupName)
                        .inviteLink("bot can't copy link")
                        .numberOfMember(1)
                        .build());
                messageText = "Привет! Спасибо, что добавили меня в группу " + groupName + " ! \n"
                        + "Для корректной работы боту нужны права администратора" + "\n"
                        + "Вы можете выдвинуть свою кандидатуру в роль модератора(старосты) - /apply@ppk_sstu_test_bot";
            }

            bot.sendMessage(chatId, messageText);

        } else if (newChatMember instanceof ChatMemberAdministrator) {
            if (existingGroup.isPresent()) {
                Group group = existingGroup.get();
                group.setChatId(chatId);
                group.setGroupTitle(groupName);

                String inviteLink = chat.getInviteLink();
                if (inviteLink == null || inviteLink.isEmpty()) {
                    CreateChatInviteLink createLink = new CreateChatInviteLink(String.valueOf(chat.getId()));
                    createLink.setName("Main invite link");
                    createLink.setCreatesJoinRequest(false);
                    try {
                        ChatInviteLink newLink = bot.execute(createLink);
                        inviteLink = newLink.getInviteLink();
                    } catch (TelegramApiException e) {
                        log.warn("ошибка при создании ссылки на инвайт: {}", e.getMessage());
                    }
                }

                group.setInviteLink(inviteLink);
                repository.save(group);

                log.debug("Бот получил администратора");

                bot.sendMessage(chatId, "Функции бота разблокированы ");
            } else {
                repository.save(Group.builder()
                        .chatId(chatId)
                        .groupId(chatId)
                        .groupTitle(groupName)
                        .inviteLink(chat.getInviteLink() == null ? "" : chat.getInviteLink())
                        .numberOfMember(1)
                        .build());
                log.debug("Бот получил админестртора ");
            }
        }
    }
}
