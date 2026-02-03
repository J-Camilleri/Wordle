package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Logic.DailyWordle;
import de.htw.saar.wordle.game.Logic.Difficulty;
import de.htw.saar.wordle.game.Logic.GameConfig;
import de.htw.saar.wordle.game.Logic.Wordle;
import de.htw.saar.wordle.game.LoginSystem.User;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Games.GAMES;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;
import static org.junit.jupiter.api.Assertions.*;

class GameRepositoryTest {

    private static final String TEST_DB = "wordle_test.db";
    private GameRepository gameRepository;
    private int testUserId;
    private User testUser;

    @BeforeAll
    static void setupClass() {
        DatabaseManager.setDbName(TEST_DB);
    }

    @BeforeEach
    void setUp() {
        try {
            DatabaseManager.dbInit();
            gameRepository = new GameRepository();

            try (Connection conn = DatabaseManager.connect()) {
                if (conn == null) fail("Keine Verbindung zur DB");
                DSLContext dsl = DSL.using(conn);

                dsl.deleteFrom(GAMES).execute();
                dsl.deleteFrom(WORDS).execute();
                dsl.deleteFrom(USERS).execute();

                dsl.insertInto(USERS)
                        .columns(USERS.USERNAME, USERS.PASSWORD_HASH)
                        .values("Tester", "hash123")
                        .execute();

                testUserId = dsl.select(USERS.ID)
                        .from(USERS)
                        .where(USERS.USERNAME.eq("Tester"))
                        .fetchOne(USERS.ID);

                testUser = new User(testUserId, "Tester", "hash123");

                dsl.insertInto(WORDS)
                        .columns(WORDS.WORD_TEXT)
                        .values("APFEL")
                        .execute();

            }

        } catch (Exception e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn != null) {
                DSLContext dsl = DSL.using(conn);
                dsl.deleteFrom(GAMES).execute();
                dsl.deleteFrom(USERS).execute();
                dsl.deleteFrom(WORDS).execute();
            }
        } catch (Exception e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }

    @Test
    void testSaveAndLoadGame() {
        GameConfig config = GameConfig.createThroughDifficulty(Difficulty.NORMAL);
        Wordle game = new Wordle(config, -1, "APFEL", List.of("TIERE"));

        boolean saved = gameRepository.saveGame(testUserId, game);
        assertTrue(saved, "Spiel sollte erfolgreich gespeichert werden");

        Optional<DailyWordle> loadedGameOpt = gameRepository.loadGame(testUserId, testUser);
        assertTrue(loadedGameOpt.isPresent(), "Gespeichertes Spiel sollte gefunden werden");

        Wordle loadedGame = loadedGameOpt.get();
        assertEquals("APFEL", loadedGame.getWordleWord());
        assertEquals(1, loadedGame.getAttempt());
        assertEquals("TIERE", loadedGame.getGuessedWords().get(0));
        assertEquals(config.getDifficulty(), loadedGame.getConfig().getDifficulty());
    }

    @Test
    void testFinishGame() {
        GameConfig config = GameConfig.createThroughDifficulty(Difficulty.EASY);
        Wordle game = new Wordle(config, -1, "APFEL", List.of());
        gameRepository.saveGame(testUserId, game);

        gameRepository.finishGame(testUserId, true);

        Optional<DailyWordle> loadedGame = gameRepository.loadGame(testUserId, testUser);
        assertTrue(loadedGame.isEmpty(), "Beendetes Spiel darf nicht mehr als aktives Spiel geladen werden");
    }

    @Test
    void testUpdateExistingGame() {
        Wordle game = new Wordle(GameConfig.createThroughDifficulty(Difficulty.NORMAL), -1, "APFEL", List.of("RATET"));
        gameRepository.saveGame(testUserId, game);

        game.checkWord("TIERE");

        boolean updated = gameRepository.saveGame(testUserId, game);
        assertTrue(updated);

        DailyWordle loaded = gameRepository.loadGame(testUserId, testUser).get();
        assertEquals(2, loaded.getAttempt());
        assertEquals("RATET", loaded.getGuessedWords().get(0));
        assertEquals("TIERE", loaded.getGuessedWords().get(1));
    }
}