package ru.yandex.practicum.exceptions.gameExceptions;

public class NonCyrillicWordException extends GameException {

    public NonCyrillicWordException(String word) {
        super("Слово \"" + word + "\" содержит недопустимые символы (разрешена только кириллица)");
    }
}
