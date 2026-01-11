package de.htw.saar.wordle.game;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private AuthenticationService auth;
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

        UserRepository repo = new UserRepository();
        auth = new AuthenticationService(repo);
    }

    @Test
    void testCreateUser() {
        auth.register("carol", "secret");
        assertTrue(auth.login("carol", "secret"));
    }

    @Test
    void testLoginUser() {
        auth.register("dave", "password");
        assertTrue(auth.login("dave", "password"));
    }

    @Test
    void testLoginWrongPassword() {
        auth.register("erin", "correct");
        assertFalse(auth.login("erin", "wrong"));
    }

    @Test
    void testLoginUserNotInDb() {
        assertFalse(auth.login("ghost", "whatever"));
    }

    @Test
    void testDeleteUser() {
        auth.register("frank", "1234");

        boolean deleted = auth.deleteAccount("frank", "1234");

        assertTrue(deleted);
        assertFalse(auth.login("frank", "1234"));
    }

    @Test
    void testDeleteUserWrongPassword() {
        auth.register("gina", "pw");

        boolean deleted = auth.deleteAccount("gina", "wrong");

        assertFalse(deleted);
        assertTrue(auth.login("gina", "pw"));
    }

    @Test
    void testDeleteUserNotInDb() {
        boolean deleted = auth.deleteAccount("nobody", "pw");
        assertFalse(deleted);
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
