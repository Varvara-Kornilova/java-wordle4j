package ru.yandex.practicum.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.TestLogUtils;
import ru.yandex.practicum.dictionary.WordleDictionary;
import ru.yandex.practicum.exceptions.gameExceptions.GameException;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {

    private PrintWriter logWriter;
    private WordleDictionary dictionary;
    private WordleGame game;

    @BeforeEach
    void setUp() {
        logWriter = TestLogUtils.createTestLogWriter();
        List<String> words = Arrays.asList("СЛОВО", "СТАРТ", "СКАЛА", "ШАРАП");
        dictionary = new WordleDictionary(words, logWriter);
        game = new WordleGame(dictionary, logWriter);
    }

    @Test
    void compareWords_shouldReturnCorrectResult() {
        assertEquals("+++++", game.compareWords("СЛОВО", "СЛОВО"));
        assertEquals("^^+++", game.compareWords("ЛСОВО", "СЛОВО"));
    }

    @Test
    void makeGuess_shouldThrowAfterMaxAttempts() throws GameException {
        for (int i = 0; i < 6; i++) {
            game.makeGuess("СЛОВО");
        }

        assertThrows(IllegalStateException.class, () -> game.makeGuess("СТАРТ"));
    }

    @Test
    void makeSuggestion_shouldReturnValidResultAndStoreSuggestion() throws GameException {
        String result = game.makeSuggestion();
        String suggestion = game.getLastSuggestion();

        assertEquals(5, result.length());
        assertTrue(result.chars().allMatch(c -> c == '+' || c == '^' || c == '-'));

        assertTrue(dictionary.getWords().contains(suggestion));
        assertEquals(result, game.compareWords(suggestion, game.getAnswer()));
    }

    @Test
    void isCorrect_shouldReturnTrueForCorrectGuess() {
        String answer = game.getAnswer();
        assertTrue(game.isCorrect(answer));
        assertTrue(game.isCorrect(answer.toLowerCase()));
    }
}