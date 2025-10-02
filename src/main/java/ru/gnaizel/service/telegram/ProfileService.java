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
        UserDto user = userService.findUserByChatId(
                update.getMessage().getChatId());
        switch (user.getUserStatus()) {
            case ACTIVE -> getDefaultProfile(user, bot);
            case ELDER -> getElderProfile(user, bot);
//            case ADMIN -> ;
            case BANED ->
                    bot.sendMessage(user.getChatId(), "Ваш профиль заблокирован \n вы можете обратится в поддержку @Gnaizel");
            case STUDENT -> getDefaultProfile(user, bot);
//            case TEACHER -> ;
        }

    }

    private void getElderProfile(UserDto user, TelegramBot bot) {
        long chatId = user.getChatId();
        String alertLevel = switch (user.getAlertLevel()) {
            case 0 -> "Отключены \uD83D\uDD15";
            case 1 -> "Только не отложные \uD83D\uDD14❗";
            case 2 -> "Все\uD83D\uDD14";
            default -> "Не задано";
        };


        String text = """
                Имя: %s
                Группа: %s
                Статус: %s
                Корпус: %s
                Уведомления: %s
                Дата регистрации: %s"""
                .formatted(user.getUserName(),
                        user.getCohort(),
                        user.getUserStatus(),
                        user.getKorpus(),
                        alertLevel,
                        user.getRegistrationDate()
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        InlineKeyboardButton editGroup = new InlineKeyboardButton("Группа✏️");
        editGroup.setCallbackData("editGroup");
        InlineKeyboardButton editKorpus = new InlineKeyboardButton("Корпус✏️");
        editKorpus.setCallbackData("editKorpus");
        InlineKeyboardButton editAlertLavel = new InlineKeyboardButton("Уведомления \uD83D\uDD14");
        editAlertLavel.setCallbackData("editAlertLevel");
        InlineKeyboardButton editSubs = new InlineKeyboardButton("Подписки на уведомления");
        editSubs.setCallbackData("editSubs");
        InlineKeyboardButton sendAlert = new InlineKeyboardButton("Сделать ананос \uD83D\uDD14");
        sendAlert.setCallbackData("sendChoseTepeAlert");
        InlineKeyboardButton groupSettings = new InlineKeyboardButton("Настройки группы \uD83D\uDD14");
        groupSettings.setCallbackData("groupSettings");

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(
                List.of(
                        List.of(editGroup, editKorpus, editAlertLavel),
                        List.of(sendAlert, editSubs),
                        List.of(groupSettings)));
        msg.setReplyMarkup(kb);

        bot.sendMessage(msg);
    }

    private void getDefaultProfile(UserDto user, TelegramBot bot) {
        long chatId = user.getChatId();
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
        InlineKeyboardButton editSubs = new InlineKeyboardButton("Подписки на уведомления");
        editSubs.setCallbackData("editSubs");

        InlineKeyboardMarkup kb = new InlineKeyboardMarkup(
                List.of(
                        List.of(editGroup, editKorpus),
                        List.of(editAlertLavel, editSubs)));
        msg.setReplyMarkup(kb);

        bot.sendMessage(msg);
    }

    public void startCommand(Update update, TelegramBot bot) {
        bot.sendMessage(MessageFactory.simple(update, "Добро пожаловать! Используйте меню ниже."));
    }
}
