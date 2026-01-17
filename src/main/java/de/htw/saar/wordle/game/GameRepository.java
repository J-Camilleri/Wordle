package de.htw.saar.wordle.game;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GameRepository {

    private static final int STATUS_LOST = 0;
    private static final int STATUS_WON = 1;
    private static final int STATUS_ACTIVE = 2;

    public boolean saveGame(int userId, Wordle game) {

        int wordId = getWordId(game.getWordleWord());
        if (wordId == -1) { // unsicher -1 richtig ist. Aber auf StackOverflow wird so gelöst
            System.out.println("Das Wort: " + game.getWordleWord() + " wurde nicht in der Datenbank gefunden.");
            return false;
        }

        int activeGameId = getActiveGameId(userId);

        // Daten vorbereiten
        String guessesString = String.join(",", game.getGuessedWords());
        String difficultyName = game.getConfig().getDifficulty().name();

        try (Connection con = DatabaseManager.connect()) {
            if (activeGameId == -1) {
                // INSERT: Neues Spiel starten
                String insertSql = """
                    INSERT INTO games (user_id, word_id, attempts_count, is_won, guesses, difficulty)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, wordId);
                    ps.setInt(3, game.getAttempt());
                    ps.setInt(4, STATUS_ACTIVE); // Markiert als "Laufend"
                    ps.setString(5, guessesString);
                    ps.setString(6, difficultyName);
                    ps.executeUpdate();
                }
            } else {
                // UPDATE: Vorhandenes Spiel aktualisieren
                String updateSql = """
                    UPDATE games 
                    SET attempts_count = ?, guesses = ?, difficulty = ?
                    WHERE id = ?
                """;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, game.getAttempt());
                    ps.setString(2, guessesString);
                    ps.setString(3, difficultyName);
                    ps.setInt(4, activeGameId);
                    ps.executeUpdate();
                }
            }
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
