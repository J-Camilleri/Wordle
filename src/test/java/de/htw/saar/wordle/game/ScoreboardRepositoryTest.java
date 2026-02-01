package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import de.htw.saar.wordle.game.Database.Score.ScoreEntry;
import de.htw.saar.wordle.game.Database.ScoreboardRepository;
import de.htw.saar.wordle.game.Database.UserRepository;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static de.htw.saar.wordle.jooq.tables.Scoreboard.SCOREBOARD;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;
import static org.junit.jupiter.api.Assertions.*;

class ScoreboardRepositoryTest {
    private static final String TEST_DB = "wordle_test.db";

    private UserRepository userRepo;
    private ScoreboardRepository scoreboardRepo;

    @BeforeEach
    void setUp() {
        DatabaseManager.setDbName(TEST_DB);

        try {
            DatabaseManager.dbInit();
        } catch (Exception e) {
            fail("DB Init fehlgeschlagen: " + e.getMessage());
        }

        try (Connection conn = DatabaseManager.connect()) {
            if (conn != null) {
                DSLContext dsl = DSL.using(conn);
                dsl.deleteFrom(SCOREBOARD).execute();
                dsl.deleteFrom(USERS).execute();
            }
        } catch (Exception e) {
            fail("Konnte Tabellen nicht leeren: " + e.getMessage());
        }

        userRepo = new UserRepository();
        scoreboardRepo = new ScoreboardRepository();
    }

    @Test
    void testUpdateScore() {
        String username = "winnerwinnerchickendinner";
        userRepo.save(username, "hash123");
        int userId = userRepo.findByUsername(username).get().id();

        ScoreboardRepository.updateScore(userId, 10);

        List<ScoreEntry> scoresAfterFirstGame = ScoreboardRepository.printScoreboard();

        ScoreEntry entry = scoresAfterFirstGame.stream()
                .filter(e -> e.username().equals(username))
                .findFirst()
                .orElseThrow();
        assertEquals(10, entry.score(), "Punkte sollten nach dem ersten Spiel 10 sein");

        ScoreboardRepository.updateScore(userId, 5);

        List<ScoreEntry> scoresAfterSecondGame = ScoreboardRepository.printScoreboard();
        ScoreEntry entry2 = scoresAfterSecondGame.stream()
                .filter(e -> e.username().equals(username))
                .findFirst()
                .orElseThrow();
        assertEquals(15, entry2.score(), "Punkte sollen addiert werden (10 + 5 = 15)");
    }

    @Test
    void printScoreboardTest() {

        assertTrue(userRepo.save("alice", "hash1"));
        assertTrue(userRepo.save("bob", "hash2"));
        assertTrue(userRepo.save("carol", "hash3"));

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) fail("Keine Verbindung zur DB");

            DSLContext dsl = DSL.using(conn);

            // bob = 10 Punkte
            dsl.update(SCOREBOARD)
                    .set(SCOREBOARD.SCORE, 10)
                    .where(SCOREBOARD.USER_ID.eq(
                            dsl.select(USERS.ID)
                                    .from(USERS)
                                    .where(USERS.USERNAME.eq("bob"))
                    ))
                    .execute();

            // carol = 3 Punkte
            dsl.update(SCOREBOARD)
                    .set(SCOREBOARD.SCORE, 3)
                    .where(SCOREBOARD.USER_ID.eq(
                            dsl.select(USERS.ID)
                                    .from(USERS)
                                    .where(USERS.USERNAME.eq("carol"))
                    ))
                    .execute();

            List<ScoreEntry> scoreboard = scoreboardRepo.printScoreboard();

            assertEquals(3, scoreboard.size());

            assertEquals("bob", scoreboard.get(0).username());
            assertEquals(10, scoreboard.get(0).score());

            assertEquals("carol", scoreboard.get(1).username());
            assertEquals(3, scoreboard.get(1).score());

            assertEquals("alice", scoreboard.get(2).username());
            assertEquals(0, scoreboard.get(2).score());

        } catch (Exception e) {
            fail("Test fehlgeschlagen: " + e.getMessage());
        }
    }
}


