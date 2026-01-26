package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.ScoreboardRepository;
import de.htw.saar.wordle.game.Database.UserRepository;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Scoreboard.SCOREBOARD;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepo;
    private ScoreboardRepository scoreboardRepo;

    private static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {
        try {
            DatabaseManager.setDbName(TEST_DB);
            File dbFile = new File(TEST_DB);
            if (dbFile.exists()) dbFile.delete();

            DatabaseManager.dbInit();

            userRepo = new UserRepository();
            scoreboardRepo = new ScoreboardRepository();

            try (Connection conn = DatabaseManager.connect()) {
                if (conn == null) fail("Keine Verbindung zur DB");
                DSLContext dsl = DSL.using(conn);
                dsl.deleteFrom(SCOREBOARD).execute();
                dsl.deleteFrom(USERS).execute();
            }
        } catch (Exception e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }
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
    void save_createsScoreboardEntryWithZeroScore() {
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
}
