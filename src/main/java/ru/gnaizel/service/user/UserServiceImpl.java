package ru.gnaizel.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gnaizel.dto.user.UserCreateDto;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.UserStatus;
import ru.gnaizel.exception.GroupValidationException;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.exception.TelegramUserByMassagValidationError;
import ru.gnaizel.exception.UserValidationError;
import ru.gnaizel.mapper.UserMapper;
import ru.gnaizel.model.Group;
import ru.gnaizel.model.User;
import ru.gnaizel.repository.GroupRepository;
import ru.gnaizel.repository.UserRepository;
import ru.gnaizel.telegram.TelegramBot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Override
    public boolean checkingForANewUserByMassage(Update update, TelegramBot bot) {
        long chatId = 0;
        long userId;
        Message message = null;
        if (update.hasCallbackQuery()) {
            return false;
        } else if (update.getMessage() == null) {
            throw new TelegramUpdateValidationError("Message not found");
        } else if (update.hasMessage()) {
            message = update.getMessage();
            chatId = message.getChatId();
        }
        userId = update.getMessage().getFrom().getId();

        String userName = message.getFrom().getUserName();
        if (userName == null || userName.isBlank()) {
            userName = message.getFrom().getFirstName();
            if (userName.isBlank()) {
                userName = message.getFrom().getLastName();
            }
        }
        chatId = message.getChatId();

        User user;
        if (!userRepository.existsByUserId((userId))) {
            user = createUser(update);

            if (chatId > 0) {
                String welcomeMessage = "Добро пожаловать " +
                        "%s" +
                        " ! \nэтот бот создан для оптимизации простых действий связаных с учёбой" +
                        "\nПока он может только прислать вам актуальное расписание. ";
                bot.sendMessage(chatId, welcomeMessage.formatted(userName));
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsIsLine = new ArrayList<>();
                List<InlineKeyboardButton> rowIsLine = new ArrayList<>();

                InlineKeyboardButton setGroupButton = new InlineKeyboardButton();
                setGroupButton.setText("✏️Группа");
                setGroupButton.setCallbackData("setGroup");

                InlineKeyboardButton setKorpusButton = new InlineKeyboardButton();
                setKorpusButton.setText("✏️Корпус");
                setKorpusButton.setCallbackData("setKorpus");

                rowIsLine.add(setGroupButton);
                rowIsLine.add(setKorpusButton);

                rowsIsLine.add(rowIsLine);

                inlineKeyboardMarkup.setKeyboard(rowsIsLine);

                bot.sendWithInlineKeyboard(chatId, "Укажите данные: Группа, Корпус", inlineKeyboardMarkup);
            }
            return true;
        } else {
            user = userRepository.findByUserId(userId).orElseThrow(() -> new UserValidationError("User not found"));
            if (chatId > 0) {
                if (user.getChatId() < 0) {
                    user.setChatId(chatId);
                    userRepository.save(user);
                }
            }
        }

        if (chatId < 0) {
            List<Group> groups = user.getGroups();
//            if (groups.stream()
//                    .filter(group -> group.getGroupId() == (chatId))) {
//
//            }
            user.getGroups().add(
                    groupRepository.findByChatId(
                                    chatId)
                            .orElseThrow(() -> new GroupValidationException("Group not found")));
            log.info(user.getGroups().toString());
            userRepository.save(user);
        }
        return false;
    }

    @Override
    public boolean setKorpus(long chatId, String korpus) {
        User user = userRepository.findByChatId(chatId)
                .orElseThrow(() -> new TelegramUserByMassagValidationError("User not found"));
        user.setKorpus(korpus);
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean setCohort(Long chatId, String cohort) {
        User user = userRepository.findByChatId(chatId)
                .orElseThrow(() -> new TelegramUserByMassagValidationError("User not found"));
        user.setCohort(cohort);
        userRepository.save(user);
        return true;
    }

    @Override
    public void setAlertLevel(long userId, byte alertLevel) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new TelegramUserByMassagValidationError("User not found"));
        user.setAlertLevel(alertLevel);
        userRepository.save(user);
    }

    @Override
    public User createUser(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();

        String userName = message.getFrom().getUserName();
        if (userName == null || userName.isBlank()) {
            userName = message.getFrom().getFirstName();
            if (userName.isBlank()) {
                userName = message.getFrom().getLastName();
            }
        }

        if (userRepository.existsByChatId(chatId)) {
            throw new UserValidationError("User is exists");
        }

        if (userName.isBlank()) {
            throw new UserValidationError("user name can't be blank");
        }
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .chatId(chatId)
                .userId(message.getFrom().getId())
                .userName(userName)
                .localDateTime(LocalDateTime.now())
                .cohort("no cohort")
                .userStatus(UserStatus.ACTIVE)
                .alertLevel((byte) 1)
                .build();
        return userRepository.save(UserMapper.userFromUserCreateDto(userCreateDto));
    }

    @Override
    public UserDto findUserByChatId(long userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserValidationError("User not found"));
        return UserMapper.userToDto(user);
    }
}
