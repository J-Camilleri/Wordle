package de.htw.saar.wordle.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreboardRepositoryTest {
    private static final String TEST_DB = "wordle_test.db";

    private UserRepository userRepo;
    private ScoreboardRepository scoreboardRepo;

    @BeforeEach
    void setUp() throws SQLException {

        DatabaseManager.setDbName(TEST_DB);
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        DatabaseManager.dbInit();
        userRepo = new UserRepository();
        scoreboardRepo = new ScoreboardRepository();
    }


    @Test
    void printScoreboardTest() throws SQLException {
        // User anlegen → Score = 0 wird automatisch erstellt
        assertTrue(userRepo.save("alice", "hash1"));
        assertTrue(userRepo.save("bob", "hash2"));
        assertTrue(userRepo.save("carol", "hash3"));

        // Scores gezielt setzen
        try (Connection con = DatabaseManager.connect()) {
            // bob = 10 Punkte
            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE scoreboard
                    SET score = 10
                    WHERE user_id = (SELECT id FROM users WHERE username = ?)
                    """)) {
                ps.setString(1, "bob");
                ps.executeUpdate();
            }

            // carol = 3 Punkte
            try (PreparedStatement ps = con.prepareStatement("""
                    UPDATE scoreboard
                    SET score = 3
                    WHERE user_id = (SELECT id FROM users WHERE username = ?)
                    """)) {
                ps.setString(1, "carol");
                ps.executeUpdate();
            }
            List<ScoreEntry> scoreboard = ScoreboardRepository.printScoreboard();

            // Assert: Größe + Sortierung + Werte prüfen
            assertEquals(3, scoreboard.size(), "Es müssen 3 Einträge im Scoreboard sein");

            assertEquals("bob", scoreboard.get(0).username());
            assertEquals(10, scoreboard.get(0).score());

            assertEquals("carol", scoreboard.get(1).username());
            assertEquals(3, scoreboard.get(1).score());

            assertEquals("alice", scoreboard.get(2).username());
            assertEquals(0, scoreboard.get(2).score());
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM scoreboard");

            stmt.execute("DELETE FROM users");

        } catch (SQLException e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }
}


