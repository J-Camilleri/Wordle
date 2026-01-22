package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.DatabaseManager;
import de.htw.saar.wordle.game.Difficulty;
import de.htw.saar.wordle.game.GameConfig;
import de.htw.saar.wordle.game.Wordle;
import de.htw.saar.wordle.jooq.tables.records.GamesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Games.GAMES;

public class GameRepository {

    private static final int STATUS_LOST = 0;
    private static final int STATUS_WON = 1;
    private static final int STATUS_ACTIVE = 2;

    public boolean saveGame(int userId, Wordle game) {
        int wordId = getWordId(game.getWordleWord());
        if (wordId == -1) return false;

        int activeGameId = getActiveGameId(userId);

        String guessesString = String.join(",", game.getGuessedWords());
        String difficultyName = game.getConfig().getDifficulty().name();

        try (Connection con = DatabaseManager.connect()) {
            DSLContext ctx = DSL.using(con);

            GamesRecord record;

            if (activeGameId == -1) {
                record = ctx.newRecord(GAMES);
            } else {
                record = ctx.fetchOne(GAMES, GAMES.ID.eq(activeGameId));
                if (record == null) return false;
            }

            record.setUserId(userId);
            record.setWordId(wordId);
            record.setAttemptsCount(game.getAttempt());
            record.setGuesses(guessesString);
            record.setDifficulty(difficultyName);
            record.setIsWon(STATUS_ACTIVE);

            record.store();

            return true;

        } catch (SQLException e) {
            System.out.println("DB Fehler beim Speichern: " + e.getMessage());
            return false;
        }
    }


    public Optional<Wordle> loadGame(int userId) {
        String sql = """
            SELECT g.id, g.guesses, g.difficulty, w.word_text
            FROM games g
            JOIN words w ON g.word_id = w.id
            WHERE g.user_id = ? AND g.is_won = ?
       """;

        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, STATUS_ACTIVE); // Nur laufende Spiele laden

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int gameId = rs.getInt("id");
                String targetWord = rs.getString("word_text");
                String guessesStr = rs.getString("guesses");
                String difficultyStr = rs.getString("difficulty");

                Difficulty diff = Difficulty.valueOf(difficultyStr);

                GameConfig config = GameConfig.createThroughDifficulty(diff);

                // String "A,B,C" -> Liste ["A", "B", "C"]
                List<String> guesses = new ArrayList<>();
                if (guessesStr != null && !guessesStr.isEmpty()) {
                    guesses = Arrays.asList(guessesStr.split(","));
                }

                return Optional.of(new Wordle(config, gameId, targetWord, guesses));
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void finishGame(int userId, boolean won) {
        String sql = "UPDATE games SET is_won = ? WHERE user_id = ? AND is_won = 2";
        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, won ? STATUS_WON : STATUS_LOST);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Fehler beim Beenden des Spiels: " + e.getMessage());
        }
    }

    // Hilfsmethoden für oben
    private int getActiveGameId(int userId) {
        String sql = "SELECT id FROM games WHERE user_id = ? AND is_won = 2";
        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    private int getWordId(String wordText) {
        String sql = "SELECT id FROM words WHERE word_text = ?";
        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, wordText);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

}
