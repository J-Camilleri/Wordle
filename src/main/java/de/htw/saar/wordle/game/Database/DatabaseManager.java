package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.Database.Words.WordSeeder;
import de.htw.saar.wordle.game.Exceptions.DataAccessException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static String url = "jdbc:sqlite:wordle.db";

    // für Tests kann man DB switchen wie Nintendo
    public static void setDbName(String dbName) {
        url = "jdbc:sqlite:" + dbName;
    }

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Fehler bei Datenbank verbindung", e);
        }
    }

    public static void dbInit() {
        try (Connection conn = connect()) {
            if (conn == null) return;

            WordSeeder.createWordTables();
            ScoreboardRepository.createScoreboard();
            UserRepository.createTable();
            DailyWordleRepository.createDailyTable();
            GameRepository.createGamesTable();
            PracticeWordleRepository.createDailyTable();

        } catch (SQLException e) {
            throw new DataAccessException("Fehler beim initialisieren der Datenabank", e);
        }
    }
}