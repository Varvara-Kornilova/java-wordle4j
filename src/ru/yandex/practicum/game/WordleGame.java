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
    private String lastSuggestion = null;

    private final Map<Integer, Character> correctPositions = new LinkedHashMap<>();
    private final Map<Character, Set<Integer>> presentLetters = new LinkedHashMap<>();
    private final Set<Character> absentLetters = new LinkedHashSet<>();

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
    }

    public String makeGuess(String guess) throws GameException {
        if (steps >= MAX_ATTEMPTS) {
            String msg = "Попытка сделана после исчерпания лимита (" + steps + " >= " + MAX_ATTEMPTS + ")";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new IllegalStateException(msg);
        }

        dictionary.validatePlayerWord(guess);

        String normalizedGuess = WordleDictionary.normalize(guess);
        String result = compareWords(normalizedGuess, answer);

        updateGameState(normalizedGuess, result);

        steps++;
        LogUtils.logInfo(logWriter, "Ход " + steps + ": игрок → '" + normalizedGuess + "' → " + result);
        return result;
    }

    public String makeSuggestion() throws GameException {
        if (steps >= MAX_ATTEMPTS) {
            String msg = "Подсказка запрошена после исчерпания лимита (" + steps + " >= " + MAX_ATTEMPTS + ")";
            LogUtils.logInfrastructureError(logWriter, msg);
            throw new IllegalStateException(msg);
        }

        List<String> candidates = dictionary.getWords().stream()
                .filter(this::isConsistentWithGameState)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            String errorMsg = "Фильтрация не дала кандидатов. Текущее состояние:\n" +
                    "  Точные: " + correctPositions + "\n" +
                    "  Присутствуют: " + presentLetters + "\n" +
                    "  Отсутствуют: " + absentLetters;
            LogUtils.logInfrastructureError(logWriter, errorMsg);
            throw new IllegalStateException("Нет подходящих слов для подсказки");
        }

        String suggestion = candidates.get(new Random().nextInt(candidates.size()));
        String result = compareWords(suggestion, answer);

        updateGameState(suggestion, result);

        steps++;
        this.lastSuggestion = suggestion;
        LogUtils.logInfo(logWriter, "Ход " + steps + ": подсказка → '" + suggestion + "' → " + result);
        return result;
    }


    private void updateGameState(String guess, String result) {
        for (int i = 0; i < guess.length(); i++) {
            char letter = guess.charAt(i);
            char feedback = result.charAt(i);

            if (feedback == '+') {
                correctPositions.put(i, letter);
                absentLetters.remove(letter);
                presentLetters.remove(letter);
            } else if (feedback == '^') {
                absentLetters.remove(letter);
                presentLetters.computeIfAbsent(letter, k -> new LinkedHashSet<>()).add(i);
            } else if (feedback == '-') {
                if (!correctPositions.containsValue(letter) && !presentLetters.containsKey(letter)) {
                    absentLetters.add(letter);
                }
            }
        }
    }

    private boolean isConsistentWithGameState(String word) {
        for (Map.Entry<Integer, Character> entry : correctPositions.entrySet()) {
            int pos = entry.getKey();
            char required = entry.getValue();

            if (word.charAt(pos) != required) {
                return false;
            }
        }

        for (char c : absentLetters) {
            if (word.indexOf(c) != -1) {
                return false;
            }
        }

        for (Map.Entry<Character, Set<Integer>> entry : presentLetters.entrySet()) {
            char letter = entry.getKey();
            Set<Integer> forbiddenPositions = entry.getValue();

            if (word.indexOf(letter) == -1) {
                return false;
            }

            for (int pos : forbiddenPositions) {
                if (word.charAt(pos) == letter) {
                    return false;
                }
            }
        }

        return true;
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
            throw new IllegalArgumentException("Длины слов не совпадают");
        }

        int n = guess.length();
        char[] result = new char[n];
        Map<Character, Integer> targetCount = new HashMap<>();

        for (char c : target.toCharArray()) {
            targetCount.put(c, targetCount.getOrDefault(c, 0) + 1);
        }

        for (int i = 0; i < n; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                result[i] = '+';
                targetCount.put(guess.charAt(i), targetCount.get(guess.charAt(i)) - 1);
            }
        }

        for (int i = 0; i < n; i++) {
            if (result[i] == 0) {
                char c = guess.charAt(i);

                if (targetCount.getOrDefault(c, 0) > 0) {
                    result[i] = '^';
                    targetCount.put(c, targetCount.get(c) - 1);
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

    public String getAnswer() {
        return answer;
    }
}
