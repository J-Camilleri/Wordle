package de.htw.saar.wordle.game;
import org.jooq.DSLContext;
import org.jooq.Record2;

import java.time.LocalDate;
import java.sql.*;

import static de.htw.saar.wordle.jooq.tables.DailyWords.DAILY_WORDS;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;


public class DailyWordleRepository implements WordProvider {


    public static void createDailyTable() {
        String dailyTableSQL = """
            CREATE TABLE IF NOT EXISTS daily_words (
                word_date TEXT PRIMARY KEY,
                word_id INTEGER NOT NULL,
                FOREIGN KEY (word_id) REFERENCES words(id)
            )
        """;

        try (Connection conn = DatabaseManager.connect()) {
            if (conn != null) {
                DSLContext dsl = org.jooq.impl.DSL.using(conn);
                dsl.execute(dailyTableSQL);
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Erstellen der daily_words-Tabelle: " + e.getMessage());
        }
    }


//    private static String today() {
//        return LocalDate.now().toString();
//    }

    public static Word checkExistingWords(String date) {
        try (var conn = DatabaseManager.connect()) {
            if (conn == null) return null;

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            Record2<Integer, String> record = dsl
                    .select(WORDS.ID, WORDS.WORD_TEXT)
                    .from(DAILY_WORDS)
                    .join(WORDS).on(DAILY_WORDS.WORD_ID.eq(WORDS.ID))
                    .where(DAILY_WORDS.WORD_DATE.eq(date))
                    .fetchOne();

            if (record != null) {
                return new Word(record.get(WORDS.ID), record.get(WORDS.WORD_TEXT));
            }
            return null;

        } catch (Exception e) {
            System.out.println("Fehler beim Prüfen der DailyWord: " + e.getMessage());
            return null;
        }
    }

    private Word getRandomWordFromDB() throws SQLException {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new SQLException("Keine Verbindung zur DB");

            DSLContext dsl = org.jooq.impl.DSL.using(conn);


            int count = dsl.fetchCount(WORDS, WORDS.IS_ACTIVE.eq(1));
            if (count == 0) throw new IllegalStateException("Keine aktiven Wörter vorhanden.");


            int offset = (int) ((System.currentTimeMillis() / 1000 / 86400) % count);

            Record2<Integer, String> record = dsl.select(WORDS.ID, WORDS.WORD_TEXT)
                    .from(WORDS)
                    .where(WORDS.IS_ACTIVE.eq(1))
                    .orderBy(WORDS.ID)
                    .limit(1)
                    .offset(offset)
                    .fetchOne();

            if (record != null) {
                return new Word(record.get(WORDS.ID), record.get(WORDS.WORD_TEXT));
            } else {
                throw new IllegalStateException("Keine aktiven Wörter gefunden.");
            }
        }
    }

    public Word chooseRandomDailyWord() throws SQLException {
        String today = LocalDate.now().toString();

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new SQLException("Keine Verbindung zur DB");
            DSLContext dsl = org.jooq.impl.DSL.using(conn);


            var record = dsl.select(WORDS.ID, WORDS.WORD_TEXT)
                    .from(DAILY_WORDS)
                    .join(WORDS).on(DAILY_WORDS.WORD_ID.eq(WORDS.ID))
                    .where(DAILY_WORDS.WORD_DATE.eq(today))
                    .fetchOne();

            if (record != null) {
                return new Word(record.get(WORDS.ID), record.get(WORDS.WORD_TEXT));
            }


            Word randomWord = getRandomWordFromDB();


            dsl.insertInto(DAILY_WORDS)
                    .columns(DAILY_WORDS.WORD_DATE, DAILY_WORDS.WORD_ID)
                    .values(today, randomWord.id())
                    .execute();

            return randomWord;
        }
    }

    @Override
    public String getRandomWord() {
        try {
            return chooseRandomDailyWord().text().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("DailyWord konnte nicht geladen/gespeichert werden", e);
        }
    }
}
