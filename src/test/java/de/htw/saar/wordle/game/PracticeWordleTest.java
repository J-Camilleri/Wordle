package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Database.Words.WordProvider;
import de.htw.saar.wordle.game.Logic.Difficulty;
import de.htw.saar.wordle.game.Logic.GameConfig;
import de.htw.saar.wordle.game.Logic.PracticeWordle;
import de.htw.saar.wordle.game.LoginSystem.User;
import de.htw.saar.wordle.jooq.tables.records.GamesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import static de.htw.saar.wordle.jooq.Tables.*;
import static org.junit.jupiter.api.Assertions.*;

class PracticeWordleTest {

    private final InputStream originalSystemIn = System.in;

    private static final int STATUS_LOST = 0;
    private static final int STATUS_WON = 1;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager.setDbName("wordle_test.db");
        DatabaseManager.dbInit();

        try (Connection conn = DatabaseManager.connect()) {
            DSLContext dsl = DSL.using(conn);

            dsl.deleteFrom(GAMES).execute();
            dsl.deleteFrom(USERS).execute();
            dsl.deleteFrom(WORDS).execute();

            dsl.insertInto(USERS)
                    .set(USERS.ID, 1)
                    .set(USERS.USERNAME, "TestUser")
                    .set(USERS.PASSWORD_HASH, "Hash")
                    .execute();

            dsl.insertInto(WORDS)
                    .set(WORDS.WORD_TEXT, "TIERE")
                    .set(WORDS.LANGUAGE_CODE, "DE")
                    .execute();
        }
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
    }

    static class MockWordProvider implements WordProvider {
        @Override
        public String getRandomWord() {
            return "TIERE";
        }
    }

    static class TestablePracticeWordle extends PracticeWordle {
        public TestablePracticeWordle(WordProvider provider, GameConfig config, User user, GameRepository gameRepo) {
            super(provider, config, user, gameRepo);
        }

        @Override
        public boolean wordExists(String userInput) {
            return true;
        }
    }

    private void checkGameInDb(int userId, int expectedAttempts, int expectedStatus) {
        try (Connection conn = DatabaseManager.connect()) {
            DSLContext dsl = DSL.using(conn);

            GamesRecord record = dsl.selectFrom(GAMES)
                    .where(GAMES.USER_ID.eq(userId))
                    .fetchOne();

            assertNotNull(record);
            assertEquals(expectedAttempts, record.getAttemptsCount());
            assertEquals(expectedStatus, record.getIsWon());

        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testWinOnFirstTryAllDifficulties() {
        for (Difficulty difficulty : Difficulty.values()) {
            String simulatedInput = "TIERE\nx\n";
            System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

            MockWordProvider wordProvider = new MockWordProvider();
            GameRepository gameRepo = new GameRepository();
            User user = new User(1, "TestUser", "Hash");
            GameConfig config = GameConfig.createThroughDifficulty(difficulty);

            TestablePracticeWordle game = new TestablePracticeWordle(wordProvider, config, user, gameRepo);

            game.gameLoop();

            assertTrue(game.gameWon());
            checkGameInDb(1, 1, STATUS_WON);

            try { setUp(); } catch (SQLException e) { fail(e); }
        }
    }

    @Test
    void testLoseAtMaxAttemptsAllDifficulties() {
        for (Difficulty difficulty : Difficulty.values()) {
            int maxAttempts = difficulty.getMaxAttempts();

            StringBuilder inputBuilder = new StringBuilder();
            for (int i = 0; i < maxAttempts; i++) {
                inputBuilder.append("APFEL\n");
            }
            inputBuilder.append("x\n");

            System.setIn(new ByteArrayInputStream(inputBuilder.toString().getBytes()));

            MockWordProvider wordProvider = new MockWordProvider();
            GameRepository gameRepo = new GameRepository();
            User user = new User(1, "TestUser", "Hash");
            GameConfig config = GameConfig.createThroughDifficulty(difficulty);

            TestablePracticeWordle game = new TestablePracticeWordle(wordProvider, config, user, gameRepo);

            game.gameLoop();

            assertTrue(game.gameLost());
            checkGameInDb(1, maxAttempts, STATUS_LOST);

            try { setUp(); } catch (SQLException e) { fail(e); }
        }
    }

    @Test
    void testWinOnLastAttemptAllDifficulties() {
        for (Difficulty difficulty : Difficulty.values()) {
            int maxAttempts = difficulty.getMaxAttempts();

            StringBuilder inputBuilder = new StringBuilder();
            for (int i = 0; i < maxAttempts - 1; i++) {
                inputBuilder.append("APFEL\n");
            }
            inputBuilder.append("TIERE\n");
            inputBuilder.append("x\n");

            System.setIn(new ByteArrayInputStream(inputBuilder.toString().getBytes()));

            MockWordProvider wordProvider = new MockWordProvider();
            GameRepository gameRepo = new GameRepository();
            User user = new User(1, "TestUser", "Hash");
            GameConfig config = GameConfig.createThroughDifficulty(difficulty);

            TestablePracticeWordle game = new TestablePracticeWordle(wordProvider, config, user, gameRepo);

            game.gameLoop();

            assertTrue(game.gameWon());
            assertFalse(game.gameLost());
            checkGameInDb(1, maxAttempts, STATUS_WON);

            try { setUp(); } catch (SQLException e) { fail(e); }
        }
    }
}