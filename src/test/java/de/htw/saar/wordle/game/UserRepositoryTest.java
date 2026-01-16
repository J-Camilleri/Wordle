package de.htw.saar.wordle.game;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepo;
    private ScoreboardRepository scoreboardRepo;

    private static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() throws SQLException {

        DatabaseManager.setDbName(TEST_DB);
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        DatabaseManager.dbInit();

//        // tabellen werden nicht automatisch von sqllite erstellt. daher:
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

        userRepo = new UserRepository();
        scoreboardRepo = new ScoreboardRepository();

    }

    @Test
    void testSaveUserAndFind() {
        userRepo.save("alice", "hash123");

        Optional<User> user = userRepo.findByUsername("alice");

        assertTrue(user.isPresent());
        assertEquals("alice", user.get().username());
    }

    @Test
    void testFindUserNotInDb() {
        Optional<User> user = userRepo.findByUsername("ghost");
        assertTrue(user.isEmpty());
    }

    @Test
    void testDeleteByUsername() {
        userRepo.save("bob", "hash");

        boolean deleted = userRepo.deleteByUsername("bob");

        assertTrue(deleted);
        assertTrue(userRepo.findByUsername("bob").isEmpty());
    }

    @Test
    void testDeleteByUsernameNotInDb() {
        boolean deleted = userRepo.deleteByUsername("nobody");
        assertFalse(deleted);
    }

    @Test
    void testRegisterUserSameName() {
        userRepo.save("carol", "hash1");
        userRepo.save("carol", "hash2"); // sollte fehlschlagen

        Optional<User> user = userRepo.findByUsername("carol");

        assertTrue(user.isPresent());
        assertEquals("carol", user.get().username());
    }

    @Test
    void save_createsScoreboardEntryWithZeroScore() throws SQLException {
        userRepo.save("carol", "hash1");
        userRepo.save("peter", "hash2");

        Optional<User> user = userRepo.findByUsername("carol");
        assertTrue(user.isPresent());

        List<ScoreEntry> list = scoreboardRepo.printScoreboard();
        ScoreEntry carolEntry = list.stream()
                .filter(e -> e.username().equals("carol"))
                .findFirst()
                .orElseThrow();

        assertEquals(0, carolEntry.score());
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
