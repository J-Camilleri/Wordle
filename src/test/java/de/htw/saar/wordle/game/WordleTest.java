package de.htw.saar.wordle.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class MockWordProvider implements WordProvider {
    @Override
    public String getRandomWord() {
        return "TIERE"; // Wort für die ganzen Tests
    }
}

class WordleTest {

    @Test
    void testCheckWord_AllGreen() {
        Wordle game = new Wordle(new MockWordProvider());

        game.checkWord("TIERE");

        char[][] letters = game.getWordleGrid();
        char[][] status = game.getWordStatusGrid();

        assertArrayEquals("TIERE".toCharArray(), letters[0]);

        for (char s : status[0]) {
            assertEquals('G', s);
        }
    }

    @Test
    void testCheckWord_SomeGreenSomeYellow() {
        Wordle game = new Wordle(new MockWordProvider());

        game.checkWord("REISE");

        char[][] status = game.getWordStatusGrid();

        assertEquals('Y', status[0][0]);
        assertEquals('Y', status[0][1]);
        assertEquals('Y', status[0][2]);
        assertEquals('X', status[0][3]);
        assertEquals('G', status[0][4]);
    }

    @Test
    void testCheckWord_AllWrong() {
        Wordle game = new Wordle(new MockWordProvider());

        game.checkWord("MANGO");

        char[][] status = game.getWordStatusGrid();


        for (char s : status[0]) {
            assertEquals('X', s);
        }
    }

    @Test
    void testCheckWord_InvalidInput() {
        Wordle game = new Wordle(new MockWordProvider());

        game.checkWord("12345");

        char[][] letters = game.getWordleGrid();
        char[][] status = game.getWordStatusGrid();


        for (char c : letters[0]) {
            assertEquals('\0', c);
        }
        for (char c : status[0]) {
            assertEquals('\0', c);
        }
    }

    @Test
    void testMultipleAttempts() {
        Wordle game = new Wordle(new MockWordProvider());

        game.checkWord("MANGO");
        game.checkWord("APFEL");

        char[][] letters = game.getWordleGrid();
        char[][] status = game.getWordStatusGrid();


        assertArrayEquals("MANGO".toCharArray(), letters[0]);
        assertArrayEquals("APFEL".toCharArray(), letters[1]);
    }

    @Test
    void testEmptyString() {
        assertFalse(Wordle.wordExists(" "), "Leeres Wort sollte false zurückgeben");
    }

    @Test
    void testWordWithSpace() {
        assertFalse(Wordle.wordExists("Te st"), "Wort mit Leerzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithNumbers() {
        assertFalse(Wordle.wordExists("Test1"), "Wort mit Zahlen sollte false zurückgeben");
    }

    @Test
    void testWordWithSpecialChars() {
        assertFalse(Wordle.wordExists("Te$st"), "Wort mit Sonderzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithUmlauts() {
        assertFalse(Wordle.wordExists("Tästs"), "Wort mit Umlaut sollte false zurückgeben");
    }

    @Test
    void testWordTooShort() {
        assertFalse(Wordle.wordExists("Test"), "Wort mit <5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testWordTooLong() {
        assertFalse(Wordle.wordExists("Testing"), "Wort mit >5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testValidWord() {
        assertTrue(Wordle.wordExists("Tests"), "Gültiges Wort sollte true zurückgeben");
    }
}