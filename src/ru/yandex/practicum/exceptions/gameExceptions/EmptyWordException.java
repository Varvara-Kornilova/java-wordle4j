package ru.yandex.practicum.exceptions.gameExceptions;

public class EmptyWordException extends GameException {

    public EmptyWordException(String word) {
        super("Слово \"" + word + "\" не может быть пустым");
    }
}
