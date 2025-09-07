package ru.gnaizel.service.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.telegram.TelegramBot;

import java.util.ArrayList;
import java.util.List;

@Component
public class MenuService {
    public void createMenuCommand(TelegramBot bot) {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/schedule_to_next_day", "Расписание на завтра"));
        botCommands.add(new BotCommand("/schedule_for_teacher", "Расписания преподавателей"));
        botCommands.add(new BotCommand("/schedule_to_day", "Расписание на сегодня"));
        botCommands.add(new BotCommand("/schedule", "Расписание на неделю"));
        botCommands.add(new BotCommand("/profile", "Профиль"));
        botCommands.add(new BotCommand("/start", "Команда для начального меню"));

        SetMyCommands myCommands = new SetMyCommands();
        myCommands.setCommands(botCommands);

        try {
            bot.execute(myCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
