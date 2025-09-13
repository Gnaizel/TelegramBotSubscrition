package ru.gnaizel.service.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MessageFactory {

    public static SendMessage simple(Update update, String text) {
        long chatId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getMessage().getChatId()
                : update.getMessage().getChatId();

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setReplyMarkup(KeyboardFactory.mainKeyboard());
        return msg;
    }

    public static SendMessage chooseKorpus(long chatId) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("Выберите 1 из корпусов ниже:");
        msg.setReplyMarkup(KeyboardFactory.korpusKeyboard());
        return msg;
    }
}