package ru.gnaizel.service.schebule;

public class ScheduleUpdatedEvent {
    private final boolean isHtmlChanged;

    public ScheduleUpdatedEvent(boolean isHtmlChanged) {
        this.isHtmlChanged = isHtmlChanged;
    }

    public boolean isHtmlChanged() {
        return isHtmlChanged;
    }
}