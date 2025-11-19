package ru.yandex.practicum.game;

import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.exceptions.gameExceptions.*;
import ru.yandex.practicum.logging.LogUtils;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;


public class WordleGame {

    private static final int MAX_ATTEMPTS = 6;
    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter logWriter;
    private final List<String> attempts;
    private final List<String> results;
    private String lastSuggestion = null;

    public WordleGame(WordleDictionary dictionary, PrintWriter logWriter) {
        this.dictionary = Objects.requireNonNull(dictionary, "Словарь не может быть null");
        this.logWriter = Objects.requireNonNull(logWriter, "PrintWriter не может быть null");

        List<String> words = dictionary.getWords();

        if (words.isEmpty()) {
            String msg = "Невозможно начать игру: словарь пуст";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new IllegalStateException(msg);
        }

        Random random = new Random();

        this.answer = words.get(random.nextInt(words.size()));
        this.steps = 0;
        this.attempts = new ArrayList<>();
        this.results = new ArrayList<>();
    }

    public String makeGuess(String guess) {
        if (steps >= MAX_ATTEMPTS) {
            String msg = "Попытка сделана после исчерпания лимита (" + steps + " >= " + MAX_ATTEMPTS + ")";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new IllegalStateException(msg);
        }

        dictionary.validatePlayerWord(guess);

        String normalizedGuess = WordleDictionary.normalize(guess);
        String result = compareWords(normalizedGuess, answer);

        attempts.add(normalizedGuess);
        results.add(result);
        steps++;

        LogUtils.logInfo(logWriter, "Ход " + steps + ": игрок → '" + normalizedGuess + "' → " + result);
        return result;
    }

    public String makeSuggestion() {
        if (steps >= MAX_ATTEMPTS) {
            String msg = "Подсказка запрошена после исчерпания лимита (" + steps + " >= " + MAX_ATTEMPTS + ")";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new IllegalStateException(msg);
        }

        List<String> candidates = new ArrayList<>(dictionary.getWords());

        for (int i = 0; i < attempts.size(); i++) {
            String attempt = attempts.get(i);
            String result = results.get(i);
            candidates = filterCompatibleWords(candidates, attempt, result);
        }

        if (candidates.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Фильтрация не дала кандидатов.\n");
            sb.append("Загадано: ").append(answer).append("\n");
            sb.append("История попыток (").append(attempts.size()).append("):\n");

            for (int i = 0; i < attempts.size(); i++) {
                sb.append("  ").append(attempts.get(i)).append(" → ").append(results.get(i)).append("\n");
            }

            String errorMsg = sb.toString();
            LogUtils.logInfrastructureError(logWriter, errorMsg);
            throw new IllegalStateException("Нет подходящих слов для подсказки");
        }

        String suggestion = candidates.get(new Random().nextInt(candidates.size()));
        String result = compareWords(suggestion, answer);

        attempts.add(suggestion);
        results.add(result);
        steps++;
        this.lastSuggestion = suggestion;

        LogUtils.logInfo(logWriter, "Ход " + steps + ": подсказка → '" + suggestion + "' → " + result);
        return result;
    }

    private List<String> filterCompatibleWords(List<String> words, String attempt, String result) {
        return words.stream()
                .filter(word -> compareWords(attempt, word).equals(result))
                .collect(Collectors.toList());
    }

    public boolean isCorrect(String guess) {
        String normalized = WordleDictionary.normalize(guess);
        return normalized.equals(answer);
    }

    public String compareWords(String guess, String target) {
        if (guess == null || target == null) {
            throw new IllegalArgumentException("Сравнение null-слов запрещено");
        }

        if (guess.length() != target.length()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Несовпадение длины: guess='").append(guess)
                    .append("' (").append(guess.length()).append("), ")
                    .append("target='").append(target)
                    .append("' (").append(target.length()).append(")");
            throw new IllegalArgumentException(sb.toString());
        }

        int n = guess.length();
        char[] result = new char[n];
        boolean[] matched = new boolean[n];

        Map<Character, Integer> targetLetterCount = new HashMap<>();

        for (char c : target.toCharArray()) {
            targetLetterCount.put(c, targetLetterCount.getOrDefault(c, 0) + 1);
        }

        for (int i = 0; i < n; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                result[i] = '+';
                matched[i] = true;
                targetLetterCount.put(guess.charAt(i), targetLetterCount.get(guess.charAt(i)) - 1);
            }
        }

        for (int i = 0; i < n; i++) {
            if (result[i] == 0) {
                char c = guess.charAt(i);

                if (targetLetterCount.getOrDefault(c, 0) > 0) {
                    result[i] = '^';
                    targetLetterCount.put(c, targetLetterCount.get(c) - 1);
                } else {
                    result[i] = '-';
                }
            }
        }

        return new String(result);
    }

    public String getLastSuggestion() {
        if (lastSuggestion == null) {
            throw new IllegalStateException("Подсказка ещё не генерировалась");
        }
        return lastSuggestion;
    }

    //для отладки
    public int getSteps() {
        return steps;
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public boolean hasAttemptsLeft() {
        return steps < MAX_ATTEMPTS;
    }

    public String getAnswer() {
        return answer; // осторожно: только для отладки!
    }
}
