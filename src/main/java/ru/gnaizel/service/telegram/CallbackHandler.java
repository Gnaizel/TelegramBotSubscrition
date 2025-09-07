package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final UserService userService;

    public void handle(Update update, TelegramBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callback = update.getCallbackQuery().getData();

        switch (callback) {
            case "setGroup", "editGroup":
                bot.sendMessage(MessageFactory.simple(update,
                        "Отправьте мне наименование вашей группы \nВ формате: ГГГ-999"));
                ProcessHandler.inProgress.put(chatId, "setGroup");
                break;
            case "setKorpus", "editKorpus":
                bot.sendMessage(MessageFactory.chooseKorpus(chatId));
                ProcessHandler.inProgress.put(chatId, "setKorpus");
                break;
            case "oneKorpusButton":
                userService.setKorpus(chatId, "Горького, 9");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Горького, 9"));
                break;
            case "tooKorpusButton":
                userService.setKorpus(chatId, "Ильинская площадь, 4");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Ильинская площадь, 4"));
                break;
            case "threeKorpusButton":
                userService.setKorpus(chatId, "Крымская, 19");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Крымская, 19"));
                break;
            case "fourKorpusButton":
                userService.setKorpus(chatId, "Международная, 24");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Международная, 24"));
                break;
            case "fiveKorpusButton":
                userService.setKorpus(chatId, "Сакко и Ванцетти, 15");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Сакко и Ванцетти, 15"));
                break;
            case "sixKorpusButton":
                userService.setKorpus(chatId, "Сакко и Ванцетти (физкультурники)");
                bot.sendMessage(MessageFactory.simple(update, "Корпус установлен: Сакко и Ванцетти (физкультурники)"));
                break;
            default:
                bot.sendMessage(MessageFactory.simple(update, "По какой-то причине кнопка не работает"));
        }
    }
}
