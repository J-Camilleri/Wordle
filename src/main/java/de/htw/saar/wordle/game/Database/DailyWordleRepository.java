package de.htw.saar.wordle.game.Database;
import de.htw.saar.wordle.game.Database.Words.Word;
import de.htw.saar.wordle.game.Database.Words.WordProvider;
import de.htw.saar.wordle.game.Exceptions.DataAccessException;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.impl.DSL;

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
        } catch (SQLException e) {
            throw new DataAccessException("Fehler beim Erstellen der daily_words-Tabelle", e);
        }
    }

    public static Word checkExistingWords(String date) {
        try (Connection conn = DatabaseManager.connect()) {
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

        } catch (SQLException e) {
            throw new DataAccessException("Fehler beim Prüfen der DailyWord: ", e);
        }
    }

    private Word getRandomWordFromDB() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new IllegalStateException("Keine Verbindung zur DB");

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            int count = dsl.fetchCount(WORDS, WORDS.IS_ACTIVE.eq(1));
            if (count == 0) throw new IllegalStateException("Keine aktiven Wörter vorhanden.");

            //TODO Nach dem Projekt auf eine andere Datenbank wechseln, damit jeder das gleiche Wort bekommt
            //TODO durch lokale Datenbank hat jeder User sein eigenes Wort auch wenn es gespeichert wird.
            Record2<Integer, String> record = dsl.select(WORDS.ID, WORDS.WORD_TEXT)
                    .from(WORDS)
                    .where(WORDS.IS_ACTIVE.eq(1))
                    .orderBy(DSL.rand())
                    .limit(1)
                    .fetchOne();

            if (record != null) {
                return new Word(record.get(WORDS.ID), record.get(WORDS.WORD_TEXT));
            } else {
                throw new IllegalStateException("Keine aktiven Wörter gefunden.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("DailyWord konnte nicht geladen werden", e);
        }
    }

    public Word chooseRandomDailyWord() {
        String today = LocalDate.now().toString();

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new IllegalStateException("Keine Verbindung zur DB");
            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            var record = dsl
                    .select(WORDS.ID, WORDS.WORD_TEXT)
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
        } catch (SQLException e) {
            throw new DataAccessException("DailyWord konnte nicht geladen oder gespeichert werden", e);
        }
    }

    @Override
    public String getRandomWord() {
        return chooseRandomDailyWord().text().toUpperCase();
    }
}
