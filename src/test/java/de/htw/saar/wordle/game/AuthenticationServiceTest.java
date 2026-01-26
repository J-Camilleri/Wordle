package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.UserRepository;
import de.htw.saar.wordle.game.LoginSystem.AuthenticationService;
import org.junit.jupiter.api.*;

import java.io.File;

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
        DatabaseManager.dbInit();

        UserRepository repo = new UserRepository();
        auth = new AuthenticationService(repo);
    }

    @Test
    void testCreateUser() {
        auth.register("carol", "secret");
        assertTrue(auth.login("carol", "secret").isPresent());
    }

    @Test
    void testLoginUser() {
        auth.register("dave", "password");
        assertTrue(auth.login("dave", "password").isPresent());
    }

    @Test
    void testLoginWrongPassword() {
        auth.register("erin", "correct");
        assertFalse(auth.login("erin", "wrong").isPresent());
    }

    @Test
    void testLoginUserNotInDb() {
        assertFalse(auth.login("ghost", "whatever").isPresent());
    }

    @Test
    void testDeleteUser() {
        auth.register("frank", "1234");

        boolean deleted = auth.deleteAccount("frank", "1234");

        assertTrue(deleted);
        assertFalse(auth.login("frank", "1234").isPresent());
    }

    @Test
    void testDeleteUserWrongPassword() {
        auth.register("gina", "pw");

        boolean deleted = auth.deleteAccount("gina", "wrong");

        assertFalse(deleted);
        assertTrue(auth.login("gina", "pw").isPresent());
    }

    @Test
    void testDeleteUserNotInDb() {
        boolean deleted = auth.deleteAccount("nobody", "pw");
        assertFalse(deleted);
    }
}
