package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Logic.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {

    @Test
    void easy_hasSevenAttempts() {
        assertEquals(7, Difficulty.EASY.getMaxAttempts());
    }

    @Test
    void normal_hasSixAttempts() {
        assertEquals(6, Difficulty.NORMAL.getMaxAttempts());
    }

    @Test
    void hard_hasFiveAttempts() {
        assertEquals(5, Difficulty.HARD.getMaxAttempts());
    }
}