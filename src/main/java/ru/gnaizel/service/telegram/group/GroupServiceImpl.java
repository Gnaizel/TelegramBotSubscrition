package ru.gnaizel.service.telegram.group;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.AlertTepe;
import ru.gnaizel.enums.UserStatus;
import ru.gnaizel.exception.GroupValidationException;
import ru.gnaizel.exception.UserValidationError;
import ru.gnaizel.model.Group;
import ru.gnaizel.model.User;
import ru.gnaizel.repository.GroupRepository;
import ru.gnaizel.repository.UserRepository;
import ru.gnaizel.service.telegram.KeyboardFactory;
import ru.gnaizel.telegram.TelegramBot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    // ConcurrentHashMap — чуть безопаснее при параллельных кликах
    private final Map<Long, Request> activeApplication = new ConcurrentHashMap<>();

    @Override
    public void getModeratorApplication(UserDto user, long chatId, TelegramBot bot) {
        long userId = user.getUserId();
        Group group = groupRepository.findByChatId(chatId)
                .orElseThrow(() -> new GroupValidationException("Group not found"));

        if (group.getModerator() == null) {

            if (activeApplication.containsKey(userId)) {
                Request request = activeApplication.get(userId);
                long secondsPassed = Duration.between(request.getCreationTime(), LocalDateTime.now())
                        .getSeconds();

                if (secondsPassed > 10800) {
                    sendApplication(user, chatId, bot);
                } else {
                    int minute = (int) ((10800 - secondsPassed) / 60);
                    bot.sendMessage(chatId,
                            (user.getUserName() + " вы не можете создать больше 1 запроса на модерацию\n" +
                                    "Попробуйте через " + minute + " минут"));
                }
            } else {
                sendApplication(user, chatId, bot);
            }
        } else {
            bot.sendMessage(chatId, "В группе уже есть модератор");
        }
    }

    @Override
    public Group findOfGroupChatId(long chatId) {
        return groupRepository.findByChatId(chatId)
                .orElseThrow(() -> new GroupValidationException("Group not found"));
    }

    private void sendApplication(UserDto user, long chatId, TelegramBot bot) {
        String messageText = String.format("%s выдвигает себя в роль модератора этой группы" +
                        "\nпроголосуйте за/против ниже.",
                user.getUserName());

        var keyboard = KeyboardFactory.createModeratorApplicationKeyBord();

        Message message = bot.sendWithInlineKeyboard(chatId, messageText, keyboard);

        Request request = new Request();
        request.setMessageId(message.getMessageId());
        request.setOriginalText(messageText);
        request.setReplyMarkup(keyboard);

        activeApplication.put(user.getUserId(), request);
    }

    @Override
    public void vote(CallbackQuery query, TelegramBot bot) {
        long queryMessageId = query.getMessage().getMessageId();
        long voterId = query.getFrom().getId();
        long chatId = query.getMessage().getChatId();

        Map.Entry<Long, Request> foundEntry = activeApplication.entrySet().stream()
                .filter(e -> e.getValue().getMessageId() == queryMessageId)
                .findFirst()
                .orElse(null);

        if (foundEntry == null) {
            bot.sendMessage(chatId, "Заявка не найдена или уже устарела.");
            return;
        }

        Long applicantUserId = foundEntry.getKey();
        Request request = foundEntry.getValue();

        synchronized (request) {
            if (request.getApprovedVoteUsers().contains(voterId)) {
                return;
            }

            request.getApprovedVoteUsers().add(voterId);

            int votesCount = request.getApprovedVoteUsers().size();
            String baseText = request.getOriginalText();

            String updatedText = baseText + "\n\n✅ Голосов за: " + votesCount;

            try {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(chatId);
                editMessage.setMessageId((int) queryMessageId);
                editMessage.setText(updatedText);
                editMessage.setReplyMarkup((InlineKeyboardMarkup) request.getReplyMarkup());

                try {
                    bot.execute(editMessage);
                } catch (Exception e) {
                    log.warn("Не удалось обновить сообщение голосования: {}", e.getMessage(), e);
                }
                if (votesCount >= 6) {
                    setGroupModerator(chatId, applicantUserId, bot);
                }
                activeApplication.put(applicantUserId, request);
            } catch (Exception e) {
                log.warn("Не удалось обновить сообщение голосования: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void devote(CallbackQuery query, TelegramBot bot) {
        long queryMessageId = query.getMessage().getMessageId();
        long voterId = query.getFrom().getId();
        long chatId = query.getMessage().getChatId();

        Map.Entry<Long, Request> foundEntry = activeApplication.entrySet().stream()
                .filter(e -> e.getValue().getMessageId() == queryMessageId)
                .findFirst()
                .orElse(null);

        if (foundEntry == null) {
            bot.sendMessage(chatId, "Заявка не найдена или уже устарела.");
            return;
        }

        Long applicantUserId = foundEntry.getKey();
        Request request = foundEntry.getValue();

        synchronized (request) {
            if (!request.getApprovedVoteUsers().contains(voterId)) {
                return;
            }

            request.getApprovedVoteUsers().remove(voterId);

            int votesCount = request.getApprovedVoteUsers().size();
            String baseText = request.getOriginalText();
            String updatedText = baseText + "\n\n✅ Голосов за: " + votesCount;

            try {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(chatId);
                editMessage.setMessageId((int) queryMessageId);
                editMessage.setText(updatedText);
                editMessage.setReplyMarkup((InlineKeyboardMarkup) request.getReplyMarkup());

                bot.execute(editMessage);
                activeApplication.put(applicantUserId, request);
            } catch (Exception e) {
                log.warn("Не удалось обновить сообщение при снятии голоса: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Group> getGroupsOfUserElder(long userId) {
        List<Group> groups = getGroupsOfUser(userId);
        List<Group> groupOfElder = groups.stream()
                .filter(group -> group.getModerator() == userId)
                .toList();

        if (groupOfElder.isEmpty()) {
            throw new GroupValidationException("User bont have groupOfElder");
        }

        return groupOfElder;
    }

    @Override
    public void sendAlertToGroup(String message, long groupChatId, long userId, TelegramBot bot) {
        Group group = groupRepository.findByChatId(groupChatId)
                .orElseThrow(() -> new GroupValidationException("Group not found"));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("User not found"));

        List<User> users = group.getUsers();

        String sendMessage = "\n" + message + "\n\nОт " + user.getUserName() + "\n<tg-spoiler>" + users.stream()
                .map(user1 -> "@" + user1.getUserName())
                .collect(Collectors.joining(" ")) + "</tg-spoiler>";

        bot.sendMessageWithHTML(groupChatId, sendMessage);

        bot.sendMessage(user.getChatId(), "Сообщение отправлено");
    }

    @Override
    public void sendAlertToUser(String message, long groupChatId, long userId, TelegramBot bot) {
        Group group = groupRepository.findByChatId(groupChatId)
                .orElseThrow(() -> new GroupValidationException("Group not found"));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("User not found"));

        List<User> users = group.getUsers();
        log.debug("users: {}", group.getUsers());


        boolean warn = false;

        for (User recipient : users) {
            long chatId = recipient.getChatId();
            log.debug("Send alert User: {}", recipient.getUserName());

            if (chatId < 0 && !warn) {
                bot.sendMessage(user.getChatId(), "Предупреждаю о том что некоторым пользователь не пришло " +
                        "сообщение так как они не писали в лс боту соответственно я не могу отправлять им сообщения ");
                warn = true;
            } else if (chatId > 0 && !recipient.getChatId().equals(user.getChatId())) {
                if (recipient.getAlertLevel() > 0) {
                    bot.sendMessage(chatId, "\n" + message + "\n\nОт " + user.getUserName() + "\nГруппа " + group.getGroupTitle());
                }
            }
        }

        bot.sendMessage(user.getChatId(), "Сообщения разосланы");
    }

    @Override
    public void sendAlertGroupMenu(long userId, AlertTepe tepe, TelegramBot bot) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("User not found"));
        List<Group> groups = getGroupsOfUserElder(userId);

        bot.sendWithInlineKeyboard(user.getChatId(), "Выберете группу в которой хотите сделать ананос: ",
                KeyboardFactory.handleAlertApplication(groups, tepe));
    }

    @Override
    public void sendChoseTepeAlert(long userId, TelegramBot bot) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("User not found"));

        bot.sendWithInlineKeyboard(user.getChatId(),
                "Выберете тип оповещения",
                KeyboardFactory.handleChoseTepeAlertApplication());
    }

    @Override
    public List<Group> getGroupsOfUser(long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("Uesr not found"));

        return user.getGroups();
    }

    @Override
    public void setGroupModerator(long chatId, long userId, TelegramBot bot) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserValidationError("User not found"));
        user.setUserStatus(UserStatus.ELDER);
        userRepository.save(user);

        Group group = groupRepository.findByChatId(chatId)
                .orElseThrow(() -> new GroupValidationException("Group not found"));
        group.setModerator(user.getUserId());
        groupRepository.save(group);

        bot.sendMessage(chatId, "Пользователь " + user.getUserName() + " становится модератором \uD83C\uDF89");

        bot.sendMessage(user.getChatId(), "Поздравляю " + user.getUserName() + " с этого момента " +
                "\nвы становитесь новым модератором группы " + group.getGroupTitle() +
                " \uD83C\uDF89\uD83C\uDF89\uD83C\uDF89" + "\nВам разблокированы функции оповещения группы " +
                "(пока-что это все функции)");
    }

    @Data
    private static class Request {
        List<Long> approvedVoteUsers = new ArrayList<>();
        LocalDateTime creationTime = LocalDateTime.now();
        long messageId;
        String originalText;
        Object replyMarkup;
    }
}