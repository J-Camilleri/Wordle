package de.htw.saar.wordle.game;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository repo;
    private static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {

        DatabaseManager.setDbName(TEST_DB);
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // tabellen werden nicht automatisch von sqllite erstellt. daher:
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(sql);

        } catch (SQLException e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }

        repo = new UserRepository();
    }

    @Test
    void testSaveUserAndFind() {
        repo.save("alice", "hash123");

        Optional<User> user = repo.findByUsername("alice");

        assertTrue(user.isPresent());
        assertEquals("alice", user.get().username());
    }

    @Test
    void testFindUserNotInDb() {
        Optional<User> user = repo.findByUsername("ghost");
        assertTrue(user.isEmpty());
    }

    @Test
    void testDeleteByUsername() {
        repo.save("bob", "hash");

        boolean deleted = repo.deleteByUsername("bob");

        assertTrue(deleted);
        assertTrue(repo.findByUsername("bob").isEmpty());
    }

    @Test
    void testDeleteByUsernameNotInDb() {
        boolean deleted = repo.deleteByUsername("nobody");
        assertFalse(deleted);
    }

    @Test
    void testRegisterUserSameName() {
        repo.save("carol", "hash1");
        repo.save("carol", "hash2"); // sollte fehlschlagen

        Optional<User> user = repo.findByUsername("carol");

        assertTrue(user.isPresent());
        assertEquals("carol", user.get().username());
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM users");

        } catch (SQLException e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }
}
