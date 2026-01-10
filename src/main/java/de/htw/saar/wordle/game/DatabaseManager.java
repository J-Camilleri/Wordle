package de.htw.saar.wordle.game; // Dein Package

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    // Pfad zur Datei im Projektordner
    private static final String URL = "jdbc:sqlite:wordle.db";

    // Stellt die Verbindung her
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            // WICHTIG: Fremdschlüssel-Check aktivieren (SQLite Standard ist aus)
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            System.out.println("Verbindung fehlgeschlagen: " + e.getMessage());
        }
        return conn;
    }

    // Beispiel: Einen neuen User anlegen
    public static boolean registerUser(String username, String password) {
        // SQL Injection verhindern: Wir nutzen Fragezeichen (?) statt Strings zusammenkleben!
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            // TODO: Hier später echtes Hashing einbauen (z.B. BCrypt), nicht Klartext!
            pstmt.setString(2, password);

            pstmt.executeUpdate();
            System.out.println("User " + username + " erfolgreich angelegt.");
            return true;

        } catch (SQLException e) {
            System.out.println("Fehler beim Registrieren: " + e.getMessage());
            return false;
        }
    }
}