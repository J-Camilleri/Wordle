package de.htw.saar.wordle.game.Logic;

public enum Difficulty {

    EASY(7),
    NORMAL(6),
    HARD(5);

    private final int maxAttempts;

    Difficulty(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
