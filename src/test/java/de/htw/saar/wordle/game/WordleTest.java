package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.Words.WordProvider;
import de.htw.saar.wordle.game.Logic.*;
import de.htw.saar.wordle.game.Presentation.Dialog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class MockWordProvider implements WordProvider {
    @Override
    public String getRandomWord() {
        return "TIERE"; // Wort für die ganzen Tests
    }
}

class WordleTest {

    private Dialog ui;

    @Test
    void testCheckWord_AllGreen() {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        game.checkWord("TIERE");
        Grid[][] board = game.getBoard();
        for (int i = 0; i < board[0].length; i++) {
            assertEquals("TIERE".charAt(i), board[0][i].letter());
            assertEquals(LetterStatus.CORRECT, board[0][i].status());
        }
    }

    @Test
    void testCheckWord_SomeGreenSomeYellow()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        game.checkWord("REISE");
        Grid[][] board = game.getBoard();
        assertEquals(LetterStatus.PRESENT, board[0][0].status());
        assertEquals(LetterStatus.PRESENT, board[0][1].status());
        assertEquals(LetterStatus.PRESENT, board[0][2].status());
        assertEquals(LetterStatus.ABSENT, board[0][3].status());
        assertEquals(LetterStatus.CORRECT, board[0][4].status());
    }

    @Test
    void testCheckWord_AllWrong() {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        game.checkWord("MANGO");
        Grid[][] board = game.getBoard();
        for (Grid cell : board[0]) {
            assertEquals(LetterStatus.ABSENT, cell.status());
        }
    }

    @Test
    void testCheckWord_InvalidInput()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        game.checkWord("12345");
        Grid[][] board = game.getBoard();
        for (Grid cell : board[0]) {
            assertNull(cell);
        }
    }

    @Test
    void testMultipleAttempts()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        game.checkWord("MANGO");
        game.checkWord("APFEL");
        Grid[][] board = game.getBoard();
        assertEquals('M', board[0][0].letter());
        assertEquals('A', board[1][0].letter());
    }

    @Test
    void testHasAttemptsLeft()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.HARD));
        for (int i = 0; i < 5; i++) {
            assertTrue(game.hasAttemptsLeft());
            game.checkWord("MANGO");
        }
        assertFalse(game.hasAttemptsLeft());
    }

    @Test
    void testHasNoAttemptsLeft()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.HARD));
        for (int i = 0; i < game.getConfig().getMaxAttempts(); i++) {
            assertTrue(game.hasAttemptsLeft());
            game.checkWord("MANGO");
        }
        assertFalse(game.hasAttemptsLeft());
    }


    @Test
    void testEmptyString()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists(" "), "Leeres Wort sollte false zurückgeben");
    }

    @Test
    void testWordWithSpace()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Te st"), "Wort mit Leerzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithNumbers()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Test1"), "Wort mit Zahlen sollte false zurückgeben");
    }

    @Test
    void testWordWithSpecialChars() {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Te$st"), "Wort mit Sonderzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithUmlauts()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Tästs"), "Wort mit Umlaut sollte false zurückgeben");
    }

    @Test
    void testWordTooShort()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Test"), "Wort mit <5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testWordTooLong()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertFalse(game.wordExists("Testing"), "Wort mit >5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testValidWord()  {
        Wordle game = new Wordle(new MockWordProvider(), GameConfig.createThroughDifficulty(Difficulty.NORMAL));
        assertTrue(game.wordExists("Tests"), "Gültiges Wort sollte true zurückgeben");
    }

    @Test
    void testGameWon_FirstTry() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.NORMAL));

        game.checkWord("TIERE");

        assertTrue(game.gameWon());
    }

    @Test
    void testGameWon_FalseWhenWrongWord() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.NORMAL));

        game.checkWord("MANGO");

        assertFalse(game.gameWon());
    }

    @Test
    void testGameWon_NoAttempts() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.NORMAL));

        assertFalse(game.gameWon());
    }

    @Test
    void testGameLost_AllAttemptsUsed() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.HARD));

        for (int i = 0; i < game.getConfig().getMaxAttempts(); i++) {
            game.checkWord("MANGO");
        }

        assertTrue(game.gameLost());
    }

    @Test
    void testGameLost_FalseWhenWon() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.NORMAL));

        game.checkWord("TIERE");

        assertFalse(game.gameLost());
    }

    @Test
    void testGameLost_AttemptsLeft() {
        Wordle game = new Wordle(new MockWordProvider(),
                GameConfig.createThroughDifficulty(Difficulty.NORMAL));

        game.checkWord("MANGO");

        assertFalse(game.gameLost());
    }
}