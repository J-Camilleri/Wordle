package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameRepositoryTest {

    private static final String TEST_DB = "wordle_test.db";
    private GameRepository gameRepository;
    private int testUserId;

    @BeforeAll
    static void setupClass() {
        DatabaseManager.setDbName(TEST_DB);
    }

    @BeforeEach
    void setUp() throws SQLException {
        gameRepository = new GameRepository();

        DatabaseManager.dbInit();

        try (Connection con = DatabaseManager.connect();
             Statement stmt = con.createStatement()) {

            // tabellen erstellen zum testen
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    word_id INTEGER,
                    attempts_count INTEGER,
                    is_won INTEGER,
                    guesses TEXT,
                    difficulty TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id),
                    FOREIGN KEY(word_id) REFERENCES words(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    word_text TEXT NOT NULL UNIQUE,
                    language_code TEXT DEFAULT 'de',
                    is_active INTEGER DEFAULT 1
                );
            """);

            stmt.execute("DELETE FROM games");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM words");

            stmt.execute("INSERT INTO users (username, password_hash) VALUES ('Tester', 'hash123')");
            testUserId = stmt.getConnection().prepareStatement("SELECT id FROM users WHERE username='Tester'").executeQuery().getInt("id");

            stmt.execute("INSERT INTO words (word_text) VALUES ('APFEL')");

        }
    }

    @AfterEach
    void tearDown() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    void testSaveAndLoadGame() {
        GameConfig config = GameConfig.createThroughDifficulty(Difficulty.NORMAL);
        Wordle game = new Wordle(config, -1, "APFEL", List.of("TIERE"));

        boolean saved = gameRepository.saveGame(testUserId, game);
        assertTrue(saved, "Spiel sollte erfolgreich gespeichert werden");

        Optional<Wordle> loadedGameOpt = gameRepository.loadGame(testUserId);
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

        Optional<Wordle> loadedGame = gameRepository.loadGame(testUserId);
        assertTrue(loadedGame.isEmpty(), "Beendetes Spiel darf nicht mehr als aktives Spiel geladen werden");
    }

    @Test
    void testUpdateExistingGame() {
        Wordle game = new Wordle(GameConfig.createThroughDifficulty(Difficulty.NORMAL), -1, "APFEL", List.of("RATET"));
        gameRepository.saveGame(testUserId, game);

        game.checkWord("TIERE");

        boolean updated = gameRepository.saveGame(testUserId, game);
        assertTrue(updated);

        Wordle loaded = gameRepository.loadGame(testUserId).get();
        assertEquals(2, loaded.getAttempt());
        assertEquals("RATET", loaded.getGuessedWords().get(0));
        assertEquals("TIERE", loaded.getGuessedWords().get(1));
    }
}