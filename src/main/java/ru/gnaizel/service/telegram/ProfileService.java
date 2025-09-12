package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;

    public void getProfile(Update update, TelegramBot bot) {
        long chatId = update.getMessage().getChatId();
        UserDto user = userService.findUserByChatId(chatId);

        String alertLevel = switch (user.getAlertLevel()) {
            case 0 -> "Отключены \uD83D\uDD15";
            case 1 -> "Только не отложные \uD83D\uDD14❗";
            case 2 -> "Все\uD83D\uDD14";
            default -> "Не задано";
        };

        String text = "Имя: %s\nГруппа: %s\nСтатус: %s\nКорпус: %s\nУведомления: %s\nДата регистрации: %s"
                .formatted(user.getUserName(),
                        user.getCohort(),
                        user.getUserStatus(),
                        user.getKorpus(),
                        alertLevel,
                        user.getRegistrationDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        InlineKeyboardButton editGroup = new InlineKeyboardButton("Группа✏️");
        editGroup.setCallbackData("editGroup");
        InlineKeyboardButton editKorpus = new InlineKeyboardButton("Корпус✏️");
        editKorpus.setCallbackData("editKorpus");
        InlineKeyboardButton editAlertLavel = new InlineKeyboardButton("Уведомления \uD83D\uDD14");
        editAlertLavel.setCallbackData("editAlertLevel");

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(
                List.of(
                        List.of(editGroup, editKorpus, editAlertLavel)));
        msg.setReplyMarkup(kb);

        bot.sendMessage(msg);
    }

    public void startCommand(Update update, TelegramBot bot) {
        bot.sendMessage(MessageFactory.simple(update, "Добро пожаловать! Используйте меню ниже."));
    }
}
