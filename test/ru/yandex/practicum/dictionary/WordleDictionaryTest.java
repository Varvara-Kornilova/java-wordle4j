package ru.yandex.practicum.dictionary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.TestLogUtils;
import ru.yandex.practicum.exceptions.gameExceptions.*;
import ru.yandex.practicum.exceptions.infrastructureExceptions.EmptyDictionaryException;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {

    private PrintWriter logWriter;
    private WordleDictionary dictionary;

    @BeforeEach
    void setUp() {
        logWriter = TestLogUtils.createTestLogWriter();
        List<String> words = Arrays.asList("СЛОВО", "ПИСЬМО", "КНИГА", "БУКВА", "МОРЕ");
        dictionary = new WordleDictionary(words, logWriter);
    }

    @Test
    void normalize_shouldConvertToUppercaseAndReplaceYo() {
        assertEquals("СЛОВО", WordleDictionary.normalize("слово"));
        assertEquals("СЛЕЗА", WordleDictionary.normalize("слёза"));
        assertEquals("ПИСЬМО", WordleDictionary.normalize("письмо"));
    }

    @Test
    void validatePlayerWord_shouldThrowOnNull() {
        assertThrows(InvalidWordLengthException.class, () -> dictionary.validatePlayerWord(null));
    }

    @Test
    void validatePlayerWord_shouldThrowOnWrongLength() {
        assertThrows(InvalidWordLengthException.class, () -> dictionary.validatePlayerWord("сл"));
        assertThrows(InvalidWordLengthException.class, () -> dictionary.validatePlayerWord("словоо"));
    }

    @Test
    void validatePlayerWord_shouldThrowEmptyWordExceptionOnWhitespaceOnly() {
        EmptyWordException e = assertThrows(EmptyWordException.class,
                () -> dictionary.validatePlayerWord("     "));
        assertEquals("Слово \"     \" не может быть пустым", e.getMessage());
    }

    @Test
    void validatePlayerWord_shouldThrowOnWhitespace() {
        assertThrows(WordContainsWhitespaceException.class, () -> dictionary.validatePlayerWord("сл во"));
    }

    @Test
    void validatePlayerWord_shouldThrowOnDigits() {
        assertThrows(WordContainsDigitException.class, () -> dictionary.validatePlayerWord("сло1о"));
    }

    @Test
    void validatePlayerWord_shouldThrowOnNonCyrillic() {
        assertThrows(NonCyrillicWordException.class, () -> dictionary.validatePlayerWord("hello"));
    }

    @Test
    void validatePlayerWord_shouldThrowIfNotInDictionary() {
        assertThrows(WordNotFoundInDictionaryException.class,
                () -> dictionary.validatePlayerWord("СТОЛБ"));
    }

    @Test
    void validatePlayerWord_shouldAcceptValidWord() {
        assertDoesNotThrow(() -> dictionary.validatePlayerWord("слово"));
    }

    @Test
    void constructor_shouldFilterInvalidWords() {
        List<String> raw = Arrays.asList("слово", "12345", "hello", "пять", "сумка");
        WordleDictionary dict = new WordleDictionary(raw, logWriter);
        assertEquals(2, dict.getWords().size());
        assertTrue(dict.getWords().contains("СЛОВО"));
        assertTrue(dict.getWords().contains("СУМКА"));
    }

    @Test
    void constructor_shouldThrowOnEmptyAfterFiltering() {
        List<String> raw = Arrays.asList("123", "hello");
        assertThrows(EmptyDictionaryException.class, () -> new WordleDictionary(raw, logWriter));
    }
}