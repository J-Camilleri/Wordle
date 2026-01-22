package de.htw.saar.wordle.game;

public interface GameUI {

    String readWord();
    void gameLost(String message);
    void gameWon(String message);
}
