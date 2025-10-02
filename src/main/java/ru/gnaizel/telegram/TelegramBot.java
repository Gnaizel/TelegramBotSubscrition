package ru.gnaizel.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gnaizel.exception.TelegramUpdateValidationError;
import ru.gnaizel.service.schebule.ScheduleService;
import ru.gnaizel.service.telegram.UpdateHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;
    private final ScheduleService scheduleService;

    @Value("${telegram.bot.token}")
    private String TELEGRAM_BOT_TOKEN;

    @Value("${telegram.bot.username}")
    private String TELEGRAM_BOT_USERNAME;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            updateHandler.handle(update, this);
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    public Message sendMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения", e);
        }
    }

    public Message sendMessage(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        try {
            return execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения", e);
        }
    }

    public Message sendWithInlineKeyboard(long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setReplyMarkup(inlineKeyboard);

        try {
            return execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения с клавиатурой - " + e);
        }
    }

    public Message sendMessageWithHTML(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode(ParseMode.HTML);

        try {
            return execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения", e);
        }
    }

    public void editMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new TelegramUpdateValidationError(e.getMessage());
        }
    }

    @Scheduled(cron = "0 30 7 * * *")
    private void sendScheduleEveryDaySub() {
        scheduleService.alertNewScheduleTodayUserSub(this);
        scheduleService.alertNewScheduleTodayGroupSub(this);
    }

    @Override
    public String getBotToken() {
        return TELEGRAM_BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return TELEGRAM_BOT_USERNAME;
    }
}

