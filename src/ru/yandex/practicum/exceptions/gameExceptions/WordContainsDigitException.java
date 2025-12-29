package ru.yandex.practicum.exceptions.gameExceptions;

public class WordContainsDigitException extends GameException {

    public WordContainsDigitException(String word) {
        super("Слово не может содержать цифры: \"" + word + "\"");
    }
}
