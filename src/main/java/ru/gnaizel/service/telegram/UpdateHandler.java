package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.telegram.TelegramBot;

@Component
@RequiredArgsConstructor
public class UpdateHandler {

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final ProcessHandler processHandler;
    private final MenuService menuService;

    public void handle(Update update, TelegramBot bot) {
        menuService.createMenuCommand(bot);
        ValidationUtil.validate(update);

        if (processHandler.checkAndHandle(update, bot)) {
            return;
        }

        if (update.hasMessage()) {
            commandHandler.handle(update, bot);
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
        }
    }
}