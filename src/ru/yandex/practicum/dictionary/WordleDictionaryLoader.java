package ru.yandex.practicum.dictionary;

import ru.yandex.practicum.exceptions.infrastructureExceptions.DictionaryFileNotFoundException;
import ru.yandex.practicum.exceptions.infrastructureExceptions.InfrastructureException;
import ru.yandex.practicum.logging.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    public static WordleDictionary loadDictionaryFromFile(String fileName, PrintWriter logWriter) {
        if (fileName == null || fileName.trim().isEmpty()) {
            LogUtils.logInfrastructureError(logWriter, "Имя файла пустое");
            throw new InfrastructureException("Имя файла не может быть пустым");
        }

        Path path = Paths.get(fileName);
        List<String> rawWords = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    rawWords.add(trimmed);
                }
            }
        } catch (Exception e) {
            LogUtils.logInfrastructureError(logWriter, "Файл не найден или недоступен: " + fileName);
            e.printStackTrace(logWriter); // PrintWriter поддерживает printStackTrace!
            logWriter.flush();
            throw new DictionaryFileNotFoundException(fileName);
        }

        return new WordleDictionary(rawWords, logWriter);
    }
}
