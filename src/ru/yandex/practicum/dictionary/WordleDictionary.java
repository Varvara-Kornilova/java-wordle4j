package ru.yandex.practicum.dictionary;

import ru.yandex.practicum.exceptions.gameExceptions.*;
import ru.yandex.practicum.exceptions.infrastructureExceptions.EmptyDictionaryException;
import ru.yandex.practicum.logging.LogUtils;

import java.io.PrintWriter;
import java.util.*;

public class WordleDictionary {

    private final List<String> words;
    private static final int WORD_LENGTH = 5;
    private final PrintWriter logWriter;

    public WordleDictionary(List<String> rawWords, PrintWriter logWriter) {
        this.logWriter = logWriter;

        if (rawWords == null) {
            String msg = "Список слов не может быть null";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new EmptyDictionaryException(msg);
        }

        Set<String> uniqueWords = new HashSet<>();

        for (String word : rawWords) {
            String normalized = normalize(word);
            if (isValidWord(normalized)) {
                uniqueWords.add(normalized);
            }
        }

        if (uniqueWords.isEmpty()) {
            String msg = "Словарь пуст после фильтрации";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new EmptyDictionaryException(msg);
        }

        this.words = new ArrayList<>(uniqueWords);
    }

    public void validatePlayerWord(String input) throws GameException {
        if (input == null || input.length() != WORD_LENGTH) {
            throwAndLogGameError(new InvalidWordLengthException(input));
        }

        if (input.trim().isEmpty()) {
            throwAndLogGameError(new EmptyWordException(input));
        }

        if (input.contains(" ")) {
            throwAndLogGameError(new WordContainsWhitespaceException(input));
        }

        if (input.chars().anyMatch(Character::isDigit)) {
            throwAndLogGameError(new WordContainsDigitException(input));
        }

        String normalized = normalize(input);
        if (!isValidWord(normalized)) {
            throwAndLogGameError(new NonCyrillicWordException(input));
        }

        if (!words.contains(normalized)) {
            throwAndLogGameError(new WordNotFoundInDictionaryException(input));
        }
    }

    private void throwAndLogGameError(GameException exception) throws GameException {
        LogUtils.logGameError(logWriter, exception.getMessage());
        throw exception;
    }

    public static String normalize(String word) {
        if (word == null) return "";
        return word.toUpperCase().replace('Ё', 'Е');
    }

    private static boolean isValidWord(String word) {
        if (word == null || word.length() != WORD_LENGTH) return false;

        for (char c : word.toCharArray()) {
            if (c < 'А' || c > 'Я') return false;
        }

        return true;
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }
}
