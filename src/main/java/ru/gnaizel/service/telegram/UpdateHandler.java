package ru.gnaizel.service.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.service.telegram.group.GroupInviteHandler;
import ru.gnaizel.telegram.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateHandler {

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;
    private final ProcessHandler processHandler;
    private final GroupInviteHandler groupInviteHandler;

    public void handle(Update update, TelegramBot bot) {
        ValidationUtil.validate(update);

        if (processHandler.checkAndHandle(update, bot)) {
            return;
        }

        if (update.hasMyChatMember()) {
            groupInviteHandler.groupInviteHandle(update, bot);
            return;
        }

        if (update.hasMessage()) {
            if (update.getMessage().getChatId() > 0) {
                commandHandler.handle(update, bot);
            } else {
                commandHandler.handleGroup(update, bot);
            }
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
        } else {
            log.debug("Необработанный update: {}", update);
        }
    }
}