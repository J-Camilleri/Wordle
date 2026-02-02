package de.htw.saar.wordle.game.Logic;

import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Database.ScoreboardRepository;
import de.htw.saar.wordle.game.Presentation.Dialog;
import de.htw.saar.wordle.game.LoginSystem.User;
import de.htw.saar.wordle.game.Database.Words.WordProvider;

import java.util.List;

public class DailyWordle extends Wordle {

    private final GameRepository gameRepository;
    private final User user;
    private Dialog ui =  new Dialog();

    public DailyWordle(WordProvider provider, GameConfig config, User user, GameRepository gameRepo) {
        super(provider, config, user.id());
        this.user = user;
        this.gameRepository = gameRepo;
    }

    public DailyWordle(WordProvider provider, GameConfig config, User user, GameRepository gameRepo, int gameId, String targetWord, List<String> guesses) {
        super(config, gameId, targetWord, guesses, user.id());
        this.user = user;
        this.gameRepository = gameRepo;
    }

    public void gameLoop() {
        while (!gameWon() && !gameLost()) {
            checkWord();
            gameRepository.saveGame(user.id(), this);
        }
        if (gameWon()) {
            int points = calculatePoints();
            ui.gameWon("Du hast Gewonnen! " + points + " Punkte wurde zu deinem Score hinzugefügt.");
            if (getUserId() != -1) {
                ScoreboardRepository.updateScore(getUserId(), points);
            }
            gameRepository.finishGame(user.id(), true);
        } else {
            ui.gameLost("Keine Versuche mehr übrig. Du hast Verloren!");
            gameRepository.finishGame(user.id(), false);
        }
    }
}