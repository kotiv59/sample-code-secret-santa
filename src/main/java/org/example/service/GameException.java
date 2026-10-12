package org.example.service;

/**
 * Нарушение правил игры; сообщение показывается пользователю как есть.
 */
public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
}
