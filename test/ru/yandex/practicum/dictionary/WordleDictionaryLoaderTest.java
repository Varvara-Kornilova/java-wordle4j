package ru.yandex.practicum.dictionary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.TestLogUtils;
import ru.yandex.practicum.exceptions.infrastructureExceptions.DictionaryFileNotFoundException;
import ru.yandex.practicum.exceptions.infrastructureExceptions.InfrastructureException;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadDictionaryFromFile_shouldLoadValidFile() throws Exception {
        Path dictFile = tempDir.resolve("test_dict.txt");
        Files.write(dictFile, java.util.Arrays.asList("слово", "пять", "книга"));

        PrintWriter logWriter = TestLogUtils.createTestLogWriter();
        WordleDictionary dict = WordleDictionaryLoader.loadDictionaryFromFile(dictFile.toString(), logWriter);

        assertEquals(2, dict.getWords().size());
        assertTrue(dict.getWords().contains("СЛОВО"));
        assertTrue(dict.getWords().contains("КНИГА"));
    }

    @Test
    void loadDictionaryFromFile_shouldThrowOnNonExistentFile() {
        PrintWriter logWriter = TestLogUtils.createTestLogWriter();
        assertThrows(DictionaryFileNotFoundException.class,
                () -> WordleDictionaryLoader.loadDictionaryFromFile("nonexistent.txt", logWriter));
    }

    @Test
    void loadDictionaryFromFile_shouldThrowOnEmptyFileName() {
        PrintWriter logWriter = TestLogUtils.createTestLogWriter();
        assertThrows(InfrastructureException.class,
                () -> WordleDictionaryLoader.loadDictionaryFromFile("", logWriter));
    }
}