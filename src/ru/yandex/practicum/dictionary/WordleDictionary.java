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

    public void validatePlayerWord(String input) {
        if (input == null || input.length() != 5) {
            InvalidWordLengthException e = new InvalidWordLengthException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }

        if (input.trim().isEmpty()) {
            EmptyWordException e = new EmptyWordException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }

        if (input.contains(" ")) {
            WordContainsWhitespaceException e = new WordContainsWhitespaceException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }

        if (input.chars().anyMatch(Character::isDigit)) {
            WordContainsDigitException e = new WordContainsDigitException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }

        String normalized = normalize(input);
        if (!isValidWord(normalized)) {
            NonCyrillicWordException e = new NonCyrillicWordException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }

        if (!words.contains(normalized)) {
            WordNotFoundInDictionaryException e = new WordNotFoundInDictionaryException(input);
            LogUtils.logGameError(logWriter, e.getMessage());
            throw e;
        }
    }

    public static String normalize(String word) {
        if (word == null) return "";
        return word.toUpperCase().replace('Ё', 'Е');
    }

    private static boolean isValidWord(String word) {
        if (word == null || word.length() != WORD_LENGTH) return false;

        for (char c : word.toCharArray()) {
            if (c < 'А' || c > 'Я') return false; // кириллические 'а' и 'я'!
        }

        return true;
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }
}
