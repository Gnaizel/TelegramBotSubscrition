package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.dto.user.UserDto;
import ru.gnaizel.enums.AlertTepe;
import ru.gnaizel.service.telegram.group.GroupService;
import ru.gnaizel.service.user.UserService;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final UserService userService;
    private final GroupService groupService;
    private final KeyboardFactory keyboardFactory = new KeyboardFactory();

    public void handle(Update update, TelegramBot bot) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();
        userService.checkingForANewUserByMassage(update, bot);
        UserDto user = userService.findUserByChatId(userId);
        String callback = update.getCallbackQuery().getData();

        if (callback.startsWith("setGroupButtonForAlert")) {
            bot.sendMessage(chatId, "Отправите текст для оповещения оно прейдёт участникам группы");
            ProcessHandler.inProgress.put(user.getUserId(), callback);
            return;
        }

        switch (callback) {
            case "setGroup", "editGroup":
                bot.sendMessage(MessageFactory.simple(update,
                        "Отправьте мне наименование вашей группы \nВ формате: ГГГ-999"));
                ProcessHandler.inProgress.put(chatId, "setGroup");
                break;
            case "setKorpus", "editKorpus": // Эдит корпуса
                bot.sendMessage(MessageFactory.chooseKorpus(chatId));
//                ProcessHandler.inProgress.put(chatId, "setKorpus");
                break;
            case "sendChoseTepeAlert":
                groupService.sendChoseTepeAlert(userId, bot);
                break;
            case "sendAlertToGroupMember":
                groupService.sendAlertGroupMenu(user.getUserId(), AlertTepe.GROUP_MAMERS, bot);
                break;
            case "sendAlertToGroup":
                groupService.sendAlertGroupMenu(user.getUserId(), AlertTepe.GROUP, bot);
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
            case "editAlertLevel": // Уровень аллёртов
                bot.sendWithInlineKeyboard(chatId, "Выберите какие уведомления вам необходмы:",
                        keyboardFactory.handleAlertLevelEditor());
                ProcessHandler.inProgress.put(chatId, "editAlertLevel");
                break;
            case "setAlertLevelZero":
                userService.setAlertLevel(chatId, (byte) 0);
                bot.sendMessage(chatId, "Изменено на \uD83D\uDD15");
                break;
            case "setAlertLevelTwo":
                userService.setAlertLevel(chatId, (byte) 1);
                bot.sendMessage(chatId, "Изменено на \uD83D\uDD14❗");
                break;
            case "setAlertLevelThree":
                userService.setAlertLevel(chatId, (byte) 2);
                bot.sendMessage(chatId, "Изменено на \uD83D\uDD14");
                break;
            case "approvedApplicationOfModeration":
                groupService.vote(update.getCallbackQuery(), bot);
                break;
            case "rejectedApplicationOfModeration":
                groupService.devote(update.getCallbackQuery(), bot);
                break;
            default:
                bot.sendMessage(MessageFactory.simple(update, "По какой-то причине кнопка не работает"));
        }
    }

}
