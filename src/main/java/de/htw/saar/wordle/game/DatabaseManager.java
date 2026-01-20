package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.ScoreboardRepository;
import de.htw.saar.wordle.game.Database.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static String url = "jdbc:sqlite:wordle.db";

    // für Tests kann man DB switchen wie Nintendo
    public static void setDbName(String dbName) {
        url = "jdbc:sqlite:" + dbName;
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            System.out.println("Verbindung fehlgeschlagen: " + e.getMessage());
        }
        return conn;
    }

    public static void dbInit() throws SQLException {
        try {
            ScoreboardRepository.createScoreboard();
            UserRepository.createTable();
            DailyWordle.createDailyTable();
        } catch (Exception e) {
            System.out.println("Fehler beim initialisieren der DB" + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement prepStmt = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            prepStmt.setString(1, username);
            prepStmt.setString(2, password);
            prepStmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Fehler beim User-Anlegen: " + e.getMessage());
            return false;
        }
    }
}