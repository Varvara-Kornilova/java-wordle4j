package ru.yandex.practicum.exceptions.gameExceptions;

public class InvalidWordLengthException extends GameException {

    public InvalidWordLengthException(String word) {
        super("Слово \"" + word + "\" не состоит из 5 букв");
    }

}
