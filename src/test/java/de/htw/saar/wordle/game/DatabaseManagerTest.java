package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp(){

        DatabaseManager.setDbName(TEST_DB);


        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        DatabaseManager.dbInit();
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
}