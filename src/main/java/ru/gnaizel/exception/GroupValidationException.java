package ru.gnaizel.exception;

public class GroupValidationException extends RuntimeException {
    public GroupValidationException(String message) {
        super(message);
    }
}
