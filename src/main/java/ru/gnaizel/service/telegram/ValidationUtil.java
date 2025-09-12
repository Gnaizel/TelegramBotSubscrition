package ru.gnaizel.service.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.gnaizel.exception.TelegramUpdateValidationError;

public class ValidationUtil {
    public static void validate(Update update) {
        if (update == null) {
            throw new TelegramUpdateValidationError("Update is null");
        }

        if (update.hasCallbackQuery() || update.hasMyChatMember()) {
            return;
        }

        if (!update.hasMessage() || (!update.getMessage().hasText() && !update.getMessage().hasDocument())) {
            throw new TelegramUpdateValidationError("Message is not valid");
        }
    }
}
