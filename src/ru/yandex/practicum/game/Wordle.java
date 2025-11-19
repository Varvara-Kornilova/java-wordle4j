package ru.yandex.practicum.game;

import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.dictionary.WordleDictionaryLoader;
import ru.yandex.practicum.exceptions.gameExceptions.GameException;
import ru.yandex.practicum.exceptions.infrastructureExceptions.InfrastructureException;
import ru.yandex.practicum.logging.LogUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    private static final String LOG_FILE_NAME = "wordle.log";
    private static final String DICTIONARY_FILE_NAME = "words_ru.txt";
    private static final int MAX_ATTEMPTS = 6;

    public static void main(String[] args) {
        try (PrintWriter logWriter = new PrintWriter(
                new FileWriter(LOG_FILE_NAME, StandardCharsets.UTF_8, true))) {

            System.out.println("Добро пожаловать в игру «5 букв»!");
            LogUtils.logInfo(logWriter, "=== ЗАПУСК ИГРЫ ===");
            System.out.println("Правила:");
            System.out.println("  - Угадайте слово из 5 букв за 6 попыток.");
            System.out.println("  - '+' — буква на своём месте,");
            System.out.println("  - '^' — буква есть, но не на своём месте,");
            System.out.println("  - '-' — буквы нет в слове.");
            System.out.println("  - Нажмите Enter в любой момент, чтобы получить подсказку (будет засчитано как попытка!).");
            System.out.println("Удачи!\n");

            WordleDictionary dictionary;
            try {
                dictionary = WordleDictionaryLoader.loadDictionaryFromFile(DICTIONARY_FILE_NAME, logWriter);
            } catch (InfrastructureException e) {
                LogUtils.logInfrastructureError(logWriter, "Не удалось загрузить словарь: " + e.getMessage());
                System.err.println("❌ Критическая ошибка: " + e.getMessage());
                return;
            }

            WordleGame game = new WordleGame(dictionary, logWriter);
            LogUtils.logInfo(logWriter, "Игра создана. Хэш загаданного слова: " + game.getAnswer().hashCode());

            Scanner scanner = new Scanner(System.in);
            boolean won = false;
            int attemptsUsed = 0;

            while (attemptsUsed < MAX_ATTEMPTS && !won) {
                System.out.printf("Попытка %d/%d. Введите слово или нажмите Enter для подсказки: ",
                        attemptsUsed + 1, MAX_ATTEMPTS);
                String input = scanner.nextLine();

                if (input.isEmpty()) {
                    try {
                        String result = game.makeSuggestion();
                        String suggestion = game.getLastSuggestion();

                        System.out.println("> " + suggestion);
                        System.out.println("> " + result);

                        if (result.equals("+++++")) {
                            System.out.println("🎉 Компьютер угадал слово за вас!");
                            won = true;
                        }
                        attemptsUsed++;

                    } catch (GameException | IllegalStateException e) {
                        System.out.println("❌ Ошибка подсказки: " + e.getMessage());
                        LogUtils.logInfrastructureError(logWriter, "Ошибка в подсказке: " + e.getMessage());
                        attemptsUsed++;
                    }

                } else {
                    try {
                        String normalized = WordleDictionary.normalize(input);
                        String result = game.makeGuess(input);

                        System.out.println("> " + normalized);
                        System.out.println("> " + result);

                        if (game.isCorrect(input)) {
                            System.out.println("🎉 Поздравляем! Вы угадали слово!");
                            won = true;
                        }
                        attemptsUsed++;

                    } catch (GameException e) {
                        System.out.println("❌ " + e.getMessage());
                        System.out.println("Попробуйте снова.");
                    }
                }

                if (attemptsUsed > MAX_ATTEMPTS) {
                    String msg = "КРИТИЧЕСКАЯ ОШИБКА: attemptsUsed (" + attemptsUsed + ") > MAX_ATTEMPTS";
                    LogUtils.logInfrastructureError(logWriter, msg);
                    throw new IllegalStateException(msg);
                }
            }

            if (!won) {
                System.out.println("\n😞 Попытки закончились. Загаданное слово было: " + game.getAnswer());
                LogUtils.logInfo(logWriter, "[ИТОГ] Проигрыш! Ответ: " + game.getAnswer());
            } else {
                System.out.println("🎉 Победа!");
                LogUtils.logInfo(logWriter, "[ИТОГ] Победа! Угадано на ходу "
                        + attemptsUsed + ". Ответ: " + game.getAnswer());
            }

            scanner.close();
            LogUtils.logInfo(logWriter, "=== ИГРА ЗАВЕРШЕНА ===");

        } catch (IOException e) {
            System.err.println("❌ Не удалось создать лог-файл: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
