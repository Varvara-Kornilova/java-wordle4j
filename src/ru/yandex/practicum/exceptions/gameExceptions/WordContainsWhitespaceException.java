package ru.yandex.practicum.exceptions.gameExceptions;

public class WordContainsWhitespaceException extends GameException {

    public WordContainsWhitespaceException(String word) {
        super("Слово не может содержать пробелы: \"" + word + "\"");
    }
}
