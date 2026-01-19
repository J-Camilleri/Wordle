package de.htw.saar.wordle.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class WordSeederTest {

    public static final String TEST_DB = "wordle_test.db";

    @BeforeEach
    void setUp() {

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

        stmt.execute(sql1);
        stmt.execute(sql2);


    } catch (SQLException e) {
        fail("Setup fehlgeschlagen: " + e.getMessage());
    }
}


    @Test
    void testCheckIfEmpty() {
        try{
            WordSeeder test = new WordSeeder();
            test.fillIfEmpty();

            int wordCount = countRows("words");
            int practiceCount = countRows("practice_words");

            assertTrue(wordCount > 0, "words Tabelle sollte nicht leer sein");
            assertTrue(practiceCount > 0, "practice_words sollte nicht leer sein");
        } catch (Exception e) {
            fail("checkIfEmpty: " + e.getMessage());
        }
    }

    @Test
    void testIsEmpty() {
        try (Connection c = DatabaseManager.connect();
            Statement st = c.createStatement()) {
            //Anfangs leer
            assertTrue(WordSeeder.isEmpty(c, "words"), "words sollte am Anfang leer sein");

            //Ein Wort einfügen
            st.executeUpdate("INSERT INTO words (word_text) VALUES ('APFEL')");

            //Sollte nicht mehr leer sein
            assertFalse(WordSeeder.isEmpty(c, "words"), "words sollte nach Insert nicht mehr leer sein");
        } catch (Exception e){
            fail("isEmpty: " + e.getMessage());
        }
    }

    @Test
    void testImportFile() {
        try (Connection c = DatabaseManager.connect()) {
            WordSeeder test = new WordSeeder();
            // importFile direkt aufrufen
            test.importFile(c, "words", "words.txt");// oder "words/words.txt"
            test.importFile(c, "practice_words", "words.txt");
            //Danach muss die Tabelle gefüllt sein
            int countWords = countRows("words");
            int countPractice = countRows("practice_words");
            assertTrue(countWords > 50, "words sollte nach importFile nicht leer sein");
            assertTrue(countPractice > 50, "words_practice sollte nach importFile nicht leer sein");

        } catch (Exception e) {
            fail("importFile: " + e.getMessage());
        }
    }


//    @AfterEach
//    void tearDown() {
//        try (Connection conn = DatabaseManager.connect();
//             Statement stmt = conn.createStatement()) {
//
//            stmt.execute("DELETE FROM words");
//            stmt.execute("DELETE FROM practice_words");
//
//        } catch (SQLException e) {
//            fail("Cleanup fehlgeschlagen: " + e.getMessage());
//        }
//    }

    private int countRows(String table) throws SQLException {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {

            rs.next();
            return rs.getInt(1);
        }
    }

}