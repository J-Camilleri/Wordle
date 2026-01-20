package de.htw.saar.wordle.game;

import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DailyWordleRepositoryTest {

    public static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.setDbName(TEST_DB);


        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            String sql1 = "CREATE TABLE IF NOT EXISTS words (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "word_text TEXT NOT NULL UNIQUE," +
                    "language_code TEXT DEFAULT 'de'," +
                    "is_active INTEGER DEFAULT 1" +
                    ");";

            String sql2 = "CREATE TABLE IF NOT EXISTS practice_words (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "word_text TEXT NOT NULL UNIQUE," +
                    "language_code TEXT DEFAULT 'de'," +
                    "is_active INTEGER DEFAULT 1" +
                    ");";

            String sql3 = "CREATE TABLE IF NOT EXISTS daily_words (" +
                    "word_date TEXT PRIMARY KEY," +
                    "word_id INTEGER NOT NULL," +
                    "FOREIGN KEY (word_id) REFERENCES words(id)" +
                    ");";


            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            DailyWordleRepository.createDailyTable();


        } catch (SQLException e) {
            fail("Setup fehlgeschlagen: " + e.getMessage());
        }

        WordSeeder.fillIfEmpty();
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


    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM daily_words");

            stmt.execute("DELETE FROM words");
            stmt.execute("DELETE FROM practice_words");


        } catch (SQLException e) {
            fail("Cleanup fehlgeschlagen: " + e.getMessage());
        }
    }
}