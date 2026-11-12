package ru.mirea.matchaggregator.error;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
