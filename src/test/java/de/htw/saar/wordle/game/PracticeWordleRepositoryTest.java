package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import de.htw.saar.wordle.game.Database.PracticeWordleRepository;
import de.htw.saar.wordle.game.Database.Words.WordSeeder;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static de.htw.saar.wordle.jooq.Tables.PRACTICE_WORDLE_HISTORY;
import static org.junit.jupiter.api.Assertions.*;

class PracticeWordleRepositoryTest {

    public static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {
        DatabaseManager.setDbName(TEST_DB);

        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        DatabaseManager.dbInit();
        WordSeeder.fillIfEmpty();
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect()) {
            DSLContext dsl = DSL.using(conn);
            dsl.deleteFrom(PRACTICE_WORDLE_HISTORY).execute();
        } catch (SQLException e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }

    @Test
    void getRandomWordReturnsUppercase() {
        PracticeWordleRepository repo = new PracticeWordleRepository();

        String word = repo.getRandomWord();

        assertEquals(word.toUpperCase(), word, "PracticeWordle muss das Wort in Uppercase zurückgeben");
    }

    @Test
    void eachCallCreatesNewHistoryEntry() {
        PracticeWordleRepository repo = new PracticeWordleRepository();

        repo.getRandomWord();
        repo.getRandomWord();
        repo.getRandomWord();

        try (Connection conn = DatabaseManager.connect()) {
            DSLContext dsl = DSL.using(conn);
            int count = dsl.fetchCount(PRACTICE_WORDLE_HISTORY);

            assertEquals(3, count,
                    "Jeder Practice-Run muss einen neuen History-Eintrag erzeugen");
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void practiceModeAllowsDifferentWords() {
        PracticeWordleRepository repo = new PracticeWordleRepository();

        String first = repo.getRandomWord();
        String second = repo.getRandomWord();

        assertNotNull(first);
        assertNotNull(second);
    }
}
