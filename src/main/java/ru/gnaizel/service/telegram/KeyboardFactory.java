package ru.gnaizel.service.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.gnaizel.enums.AlertTepe;
import ru.gnaizel.model.Group;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class KeyboardFactory {

    public static ReplyKeyboardMarkup mainKeyboard() {
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

    public static InlineKeyboardMarkup createModeratorApplicationKeyBord() {
        InlineKeyboardMarkup keyBoard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton approve = new InlineKeyboardButton();
        approve.setText("✅");
        approve.setCallbackData("approvedApplicationOfModeration");

        InlineKeyboardButton rejected = new InlineKeyboardButton();
        rejected.setText("❌");
        rejected.setCallbackData("rejectedApplicationOfModeration");

        rows.add(List.of(approve, rejected));

        keyBoard.setKeyboard(rows);
        return keyBoard;
    }

    public static InlineKeyboardMarkup handleChoseTepeAlertApplication() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton groupButton = new InlineKeyboardButton();
        groupButton.setText("Отправить В группу");
        groupButton.setCallbackData("sendAlertToGroup");

        InlineKeyboardButton groupMembersButton = new InlineKeyboardButton();
        groupMembersButton.setText("Отправить участникам группы");
        groupMembersButton.setCallbackData("sendAlertToGroupMember");

        rows.add(List.of(groupButton));
        rows.add(List.of(groupMembersButton));

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup handleAlertApplication(List<Group> groups, AlertTepe tepe) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Group group : groups) {
            InlineKeyboardButton groupButton = new InlineKeyboardButton();
            groupButton.setText(group.getGroupTitle());
            groupButton.setCallbackData("setGroupButtonForAlert"
                    + (group.getChatId()) + "-" + tepe.toString());

            rows.add(List.of(groupButton));
        }

        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup handleGroupSettings(long groupId) { // Настройки группы
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton alertGroupSettings = new InlineKeyboardButton();
        alertGroupSettings.setText("Настройка оповещений");
        alertGroupSettings.setCallbackData("alertGroupSettings" + (groupId));
        log.debug(alertGroupSettings.getCallbackData());


        rows.add(List.of(alertGroupSettings));

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup handleAlertGroupSettings(long groupId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton alertGroupSettingsEveryWeekSchedule = new InlineKeyboardButton();
        alertGroupSettingsEveryWeekSchedule.setText("Еженедельное расписание");
        alertGroupSettingsEveryWeekSchedule.setCallbackData("alertGroupSettingsEveryWeekSchedule" + groupId);

        InlineKeyboardButton alertGroupSettingsEveryDaySchedule = new InlineKeyboardButton();
        alertGroupSettingsEveryDaySchedule.setText("Ежедневное расписание");
        alertGroupSettingsEveryDaySchedule.setCallbackData("alertGroupSettingsEveryDaySchedule" + groupId);

        InlineKeyboardButton groupCohort = new InlineKeyboardButton();
        groupCohort.setText("Изменить группу (ИИИ-000)");
        groupCohort.setCallbackData("setGroupCohort" + groupId);

//        InlineKeyboardButton groupKorpus = new InlineKeyboardButton();
//        groupKorpus.setText("Изменить корпус");
//        groupKorpus.setCallbackData("setGroupKorpus" + groupId);

        rows.add(List.of(groupCohort));

        rows.add(List.of(alertGroupSettingsEveryWeekSchedule));
        rows.add(List.of(alertGroupSettingsEveryDaySchedule));

        inlineKeyboard.setKeyboard(rows);
        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup handleGroupMenuForGroupSettings(List<Group> groups) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Group group : groups) {
            InlineKeyboardButton groupButton = new InlineKeyboardButton();
            groupButton.setText(group.getGroupTitle());
            groupButton.setCallbackData("groupSettings"
                    + group.getChatId());
            log.debug(groupButton.getCallbackData());

            rows.add(List.of(groupButton));
        }

        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }

    public static InlineKeyboardMarkup handleSetCohortForGroup(long groupId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton groupCohort = new InlineKeyboardButton();
        groupCohort.setText("Изменить группу (ИИИ-000)");
        groupCohort.setCallbackData("setGroupCohort" + groupId);

        InlineKeyboardButton groupKorpus = new InlineKeyboardButton();
        groupKorpus.setText("Изменить корпус");
        groupKorpus.setCallbackData("setGroupKorpus" + groupId);

        rows.add(List.of(groupCohort));
        rows.add(List.of(groupKorpus));

        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }

    public InlineKeyboardMarkup handleSubEditor() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton everyDay = new InlineKeyboardButton();
        everyDay.setText("Ежедневное Расписание");
        everyDay.setCallbackData("setScheduleSubDay");

        InlineKeyboardButton everyWeek = new InlineKeyboardButton();
        everyWeek.setText("Еженедельное Расписание");
        everyWeek.setCallbackData("setScheduleSubWeek");

        rows.add(List.of(everyDay));
        rows.add(List.of(everyWeek));

        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }

    public InlineKeyboardMarkup handleAlertLevelEditor() {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton zero = new InlineKeyboardButton();
        zero.setText("Выкл");
        zero.setCallbackData("setAlertLevelZero");

        InlineKeyboardButton two = new InlineKeyboardButton();
        two.setText("Важные");
        two.setCallbackData("setAlertLevelTwo");

        InlineKeyboardButton three = new InlineKeyboardButton();
        three.setText("Все");
        three.setCallbackData("setAlertLevelThree");

        rows.add(List.of(zero));
        rows.add(List.of(two));
        rows.add(List.of(three));

        inlineKeyboard.setKeyboard(rows);

        return inlineKeyboard;
    }
}
