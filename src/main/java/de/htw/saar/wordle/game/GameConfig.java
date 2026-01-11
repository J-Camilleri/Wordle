package de.htw.saar.wordle.game;

public class GameConfig {

    private final int maxAttempts;
    private final int wordlength;
    private final String language;
    private final Difficulty difficulty;

    private GameConfig(int maxAttempts, int wordlength, String language, Difficulty difficulty) {
        this.maxAttempts = maxAttempts;
        this.wordlength = wordlength;
        this.language = language;
        this.difficulty = difficulty;
    }

    public static GameConfig createThroughDifficulty(Difficulty difficulty) {
        return new GameConfig(difficulty.getMaxAttempts(), 5, "de-DE", difficulty);
    }


    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getLanguage() {
        return language;
    }

    public int getWordlength() {
        return wordlength;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

}
