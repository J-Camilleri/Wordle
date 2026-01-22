package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Presentation.Dialog;

public class DailyWordle extends Wordle {

    private final GameRepository gameRepository;
    private final User user;
    private Dialog ui =  new Dialog();

    public DailyWordle(WordProvider provider, GameConfig config, User user, GameRepository gameRepo) {
        super(provider, config);
        this.user = user;
        this.gameRepository = gameRepo;
    }

    public void gameLoop() {
        while (!gameWon() && !gameLost()) {
            checkWord();
            gameRepository.saveGame(user.id(), this);
        }

        if (gameWon()) {
            ui.gameWon("Du hast Gewonnen! Hurra!");
        } else {
            ui.gameLost("Keine Versuche mehr übrig. Du hast Verloren!");
        }
    }
}
