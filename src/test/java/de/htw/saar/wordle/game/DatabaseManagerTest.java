package de.htw.saar.wordle.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    // seperate test db müssen ma noch anlegen
    private static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() throws SQLException {

        DatabaseManager.setDbName(TEST_DB);


        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseManager.dbInit();


        // tabellen werden nicht automatisch von sqllite erstellt. daher:
//        try (Connection conn = DatabaseManager.connect();
//             Statement stmt = conn.createStatement()) {
//
//            String sql = "CREATE TABLE IF NOT EXISTS users (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    "username TEXT NOT NULL UNIQUE," +
//                    "password_hash TEXT NOT NULL," +
//                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
//                    ");";
//            stmt.execute(sql);
//
//        } catch (SQLException e) {
//            fail("Setup fehlgeschlagen: " + e.getMessage());
//        }
    }

    @Test
    void testConnection() {
        Connection conn = DatabaseManager.connect();
        assertNotNull(conn, "Die Verbindung sollte nicht null sein");
        try {
            assertFalse(conn.isClosed(), "Die Verbindung sollte offen sein");
            conn.close();
        } catch (SQLException e) {
            fail("SQL Fehler beim Prüfen der Verbindung");
        }
    }

    @Test
    void testRegisterUserSuccess() {
        boolean result = DatabaseManager.registerUser("JUnitPlayer", "geheim123");

        assertTrue(result, "User sollte erfolgreich angelegt werden");
    }

    @Test
    void testRegisterUserDuplicateFail() {
        DatabaseManager.registerUser("DoppelterPeter", "pw1");

        boolean result = DatabaseManager.registerUser("DoppelterPeter", "pw2");

        assertFalse(result, "Der zweite User mit gleichem Namen sollte abgelehnt werden");
    }
}