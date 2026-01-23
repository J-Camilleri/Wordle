package de.htw.saar.wordle.game;

import de.htw.saar.wordle.jooq.tables.PracticeWords;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.*;

import static de.htw.saar.wordle.jooq.tables.PracticeWords.PRACTICE_WORDS;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;
import static org.junit.jupiter.api.Assertions.*;

class WordSeederTest {

    public static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {
        DatabaseManager.setDbName(TEST_DB);

        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) dbFile.delete();

        try {
            DatabaseManager.dbInit();
        } catch (Exception e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }
    }



    @Test
    void testCheckIfEmpty() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) fail("Keine Verbindung zur DB");
            DSLContext dsl = DSL.using(conn);
            WordSeeder.fillIfEmpty();

            int wordCount = countRows(dsl, WORDS);
            int practiceCount = countRows(dsl, PRACTICE_WORDS);

            assertTrue(wordCount > 0, "words Tabelle sollte nicht leer sein");
            assertTrue(practiceCount > 0, "practice_words Tabelle sollte nicht leer sein");

        } catch (Exception e) {
            fail("checkIfEmpty: " + e.getMessage());
        }
    }

    @Test
    void testIsEmpty() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) fail("Keine Verbindung zur DB");
            DSLContext dsl = DSL.using(conn);

            assertTrue(WordSeeder.isEmpty(dsl, WORDS), "words sollte am Anfang leer sein");

            dsl.insertInto(WORDS)
                    .columns(WORDS.WORD_TEXT)
                    .values("APFEL")
                    .execute();

            assertFalse(WordSeeder.isEmpty(dsl, WORDS), "words sollte nach Insert nicht mehr leer sein");

        } catch (Exception e) {
            fail("isEmpty: " + e.getMessage());
        }
    }

    @Test
    void testImportFile() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) fail("Keine Verbindung zur DB");
            DSLContext dsl = DSL.using(conn);

            WordSeeder.importFile(dsl, WORDS, "words.txt");

            int countWords = countRows(dsl, WORDS);

            assertTrue(countWords > 50, "words sollte nach importFile nicht leer sein");

        } catch (Exception e) {
            fail("importFile: " + e.getMessage());
        }
    }


    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return;
            DSLContext dsl = DSL.using(conn);

            dsl.deleteFrom(WORDS).execute();

        } catch (Exception e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }

    private int countRows(DSLContext dsl, org.jooq.Table<?> table) {
        return dsl.fetchCount(table);
    }

}