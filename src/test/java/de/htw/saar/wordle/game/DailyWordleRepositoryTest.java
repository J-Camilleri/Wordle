package de.htw.saar.wordle.game;

import java.time.LocalDate;

import de.htw.saar.wordle.game.Database.DailyWordleRepository;
import de.htw.saar.wordle.game.Database.DatabaseManager;
import de.htw.saar.wordle.game.Database.Words.Word;
import de.htw.saar.wordle.game.Database.Words.WordSeeder;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static de.htw.saar.wordle.jooq.tables.DailyWords.DAILY_WORDS;
import static org.junit.jupiter.api.Assertions.*;

class DailyWordleRepositoryTest {

    public static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {
        DatabaseManager.setDbName(TEST_DB);


        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        try{
            DatabaseManager.dbInit();
        } catch (Exception e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }

        WordSeeder.fillIfEmpty();
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn != null) {
                DSLContext dsl = DSL.using(conn);
                dsl.deleteFrom(DAILY_WORDS).execute();
            }
        } catch (Exception e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }


    @Test
    void checkExistingWords() throws SQLException {
        String today = LocalDate.now().toString();
        Word w = DailyWordleRepository.checkExistingWords(today);

        assertNull(w,"Es sollte noch kein DailyWord für heute existieren");
    }

    @Test
    void chooseRandomDailyWord() throws SQLException {
        DailyWordleRepository dw = new DailyWordleRepository();
        Word first = dw.chooseRandomDailyWord();
        Word second = dw.chooseRandomDailyWord(); //muss gleich sein

        assertEquals(first.id(), second.id(), "Am selben Tag muss die gleiche word_id zurückkommen");
        assertEquals(first.text(), second.text(), "Am selben Tag muss der gleiche Text zurückkommen");
    }

    @Test
    void getRandomWord() throws SQLException {
        DailyWordleRepository dw = new DailyWordleRepository();
        String word = dw.getRandomWord();

        assertEquals(word.toUpperCase(), word, "getRandomWord() sollte Uppercase zurückgeben");
    }
}