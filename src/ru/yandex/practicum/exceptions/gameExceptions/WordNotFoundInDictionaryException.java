package ru.yandex.practicum.exceptions.gameExceptions;

public class WordNotFoundInDictionaryException extends GameException {

    public WordNotFoundInDictionaryException(String word) {
        super("Слово \"" + word + "\" отсутствует в словаре");
    }
}
