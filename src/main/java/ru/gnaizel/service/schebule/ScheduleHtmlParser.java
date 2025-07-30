package ru.gnaizel.service.schebule;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.gnaizel.exception.ScheduleValidationError;
import ru.gnaizel.model.ScheduleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ScheduleHtmlParser {

    public static String extractTeacherBlock(String html) {
        Pattern pattern = Pattern.compile("let teachers\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            throw new ScheduleValidationError("'teachers' block not found");
        }
        log.info(matcher.toString());
        return matcher.group(1);
    }

    public static String extractStudentsBlock(String html) {
        Pattern pattern = Pattern.compile("let students\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            throw new ScheduleValidationError("'Students' block not found");
        }
        return matcher.group(1);
    }

    public static List<ScheduleEntry> parseSchedule(String studentsBlock, String groupName) throws ScheduleValidationError {
        List<ScheduleEntry> result = new ArrayList<>();
        boolean groupFound = false;

        Pattern buildingPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{(.*?)\\}(,?)", Pattern.DOTALL);
        Matcher buildingMatcher = buildingPattern.matcher(studentsBlock);

        while (buildingMatcher.find()) {
            String building = buildingMatcher.group(1);
            String groupsBlock = buildingMatcher.group(2);

            Pattern groupPattern = Pattern.compile("\"[^\"]*?" + Pattern.quote(groupName) + "[^\"]*?\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Matcher groupMatcher = groupPattern.matcher(groupsBlock);

            if (groupMatcher.find()) {
                groupFound = true;
                String scheduleArrayRaw = groupMatcher.group(1);
                result.addAll(parseScheduleTable(scheduleArrayRaw, building));
            }
        }

        if (!groupFound) {
            throw new ScheduleValidationError("Group '" + groupName + "' not found");
        }

        return result;
    }

    private static List<ScheduleEntry> parseScheduleTable(String raw, String building) {
        List<ScheduleEntry> entries = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return entries;
        }

        // Обработка и очистка HTML-строк
        String cleanHtml = raw
                .replaceAll("\\\\\"", "\"")  // Убираем экранирование кавычек
                .replaceAll("\\\\/", "/")    // Убираем экранирование слэшей
                .replaceAll("\\\\\\\\", "\\\\"); // Убираем двойное экранирование

        // Собираем полную HTML-таблицу
        String fullHtml = "<table>" + cleanHtml + "</table>";
        Document doc = Jsoup.parse(fullHtml);
        Elements rows = doc.select("tr");

        // Обработка заголовков (первая строка)
        Elements headerCells = rows.get(0).select("td");
        List<String> headers = new ArrayList<>();
        for (int i = 2; i < headerCells.size(); i++) {
            headers.add(headerCells.get(i).text().trim());
        }

        // Обработка данных (начиная с третьей строки)
        for (int i = 2; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");

            if (cells.size() < 3) continue;

            // Обработка номера пары и времени
            String lessonNumber = cells.get(0).text().trim();
            String time = formatTime(cells.get(1).html()); // Используем HTML для корректного парсинга времени

            // Обработка ячеек с занятиями
            for (int j = 2; j < cells.size(); j++) {
                String day = j - 2 < headers.size() ? headers.get(j - 2) : "Unknown";
                String content = cells.get(j).text().trim();

                if (content.isEmpty()) {
                    content = "—";
                }

                ScheduleEntry entry = new ScheduleEntry();
                entry.setBuilding(building);
                entry.setDay(day);
                entry.setLessonNumber(lessonNumber);
                entry.setTime(time);
                entry.setContent(content);
                entries.add(entry);
            }
        }

        return entries;
    }

    private static String formatTime(String html) {
        // Обрабатываем формат времени с разделителем <hr>
        return html
                .replaceAll("<hr[^>]*>", " - ") // Заменяем <hr> на разделитель
                .replaceAll("&nbsp;", " ")       // Заменяем HTML-пробелы
                .replaceAll("\\s+", " ")         // Убираем множественные пробелы
                .trim();
    }
}