package de.htw.saar.wordle.game;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    @Test
    void testEmptyString() throws IOException {
        assertFalse(Wordle.wordExists(" "), "Leeres Wort sollte false zurückgeben");
    }

    @Test
    void testWordWithSpace() throws IOException {
        assertFalse(Wordle.wordExists("Te st"), "Wort mit Leerzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithNumbers() throws IOException {
        assertFalse(Wordle.wordExists("Test1"), "Wort mit Zahlen sollte false zurückgeben");
    }

    @Test
    void testWordWithSpecialChars() throws IOException {
        assertFalse(Wordle.wordExists("Te$st"), "Wort mit Sonderzeichen sollte false zurückgeben");
    }

    @Test
    void testWordWithUmlauts() throws IOException {
        assertFalse(Wordle.wordExists("Tästs"), "Wort mit Umlaut sollte false zurückgeben");
    }

    @Test
    void testWordTooShort() throws IOException {
        assertFalse(Wordle.wordExists("Test"), "Wort mit <5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testWordTooLong() throws IOException {
        assertFalse(Wordle.wordExists("Testing"), "Wort mit >5 Buchstaben sollte false zurückgeben");
    }

    @Test
    void testValidWord() throws IOException {
        assertTrue(Wordle.wordExists("Tests"), "Gültiges Wort sollte true zurückgeben");
    }
}