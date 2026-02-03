package de.htw.saar.wordle.game.Logic;

import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Presentation.Dialog;
import de.htw.saar.wordle.game.LoginSystem.User;
import de.htw.saar.wordle.game.Database.Words.WordProvider;

public class PracticeWordle extends Wordle {

    private final GameRepository gameRepository;
    private final User user;
    private Dialog ui =  new Dialog();

    public PracticeWordle(WordProvider provider, GameConfig config, User user, GameRepository gameRepo) {
        super(provider, config, user.id());
        this.user = user;
        this.gameRepository = gameRepo;
    }

    public void gameLoop() {
        while (!gameWon() && !gameLost()) {
            checkWord();
            gameRepository.saveGame(user.id(), this);
        }
        if (gameWon()) {
            ui.gameWon("Du hast Gewonnen!");
            gameRepository.finishGame(user.id(), true);
        } else {
            ui.gameLost("Keine Versuche mehr übrig. Du hast Verloren!");
            gameRepository.finishGame(user.id(), false);
        }
    }
}
