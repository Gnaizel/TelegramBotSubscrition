package ru.gnaizel.service.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static ReplyKeyboardMarkup mainKeyboard(Update update) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row1.add("Расписание 📅");
        row1.add("Расписание преподавателей 📅");

        row2.add("Профиль 🙎‍♂️");

        rows.add(row1);
        rows.add(row2);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup korpusKeyboard() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton one = new InlineKeyboardButton("Горького, 9");
        one.setCallbackData("oneKorpusButton");

        InlineKeyboardButton two = new InlineKeyboardButton("Ильинская площадь, 4");
        two.setCallbackData("tooKorpusButton");

        InlineKeyboardButton three = new InlineKeyboardButton("Крымская, 19");
        three.setCallbackData("threeKorpusButton");

        InlineKeyboardButton four = new InlineKeyboardButton("Международная, 24");
        four.setCallbackData("fourKorpusButton");

        InlineKeyboardButton five = new InlineKeyboardButton("Сакко и Ванцетти, 15");
        five.setCallbackData("fiveKorpusButton");

        InlineKeyboardButton six = new InlineKeyboardButton("Сакко и Ванцетти (физкультурники)");
        six.setCallbackData("sixKorpusButton");

        rows.add(List.of(one, two));
        rows.add(List.of(three, four));
        rows.add(List.of(five, six));

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }
}
