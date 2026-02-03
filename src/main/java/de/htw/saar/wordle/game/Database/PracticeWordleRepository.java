package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.Database.Words.Word;
import de.htw.saar.wordle.game.Database.Words.WordProvider;
import de.htw.saar.wordle.game.Exceptions.DataAccessException;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static de.htw.saar.wordle.jooq.Tables.PRACTICE_WORDLE_HISTORY;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;

public class PracticeWordleRepository implements WordProvider {

    public static void createDailyTable() {
        String dailyTableSQL = """
            CREATE TABLE IF NOT EXISTS practice_wordle_history (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            word_id INTEGER NOT NULL,
            played_at TEXT NOT NULL,

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

    private Word getRandomWordFromDB() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) {
                throw new IllegalStateException("Keine Verbindung zur DB");
            }

            DSLContext dsl = DSL.using(conn);

            Record2<Integer, String> record = dsl
                    .select(WORDS.ID, WORDS.WORD_TEXT)
                    .from(WORDS)
                    .where(WORDS.IS_ACTIVE.eq(1))
                    .orderBy(DSL.rand())
                    .limit(1)
                    .fetchOne();

            if (record == null) {
                throw new IllegalStateException("Kein aktives Wort gefunden.");
            }

            return new Word(
                    record.get(WORDS.ID),
                    record.get(WORDS.WORD_TEXT)
            );

        } catch (SQLException e) {
            throw new DataAccessException("PracticeWord konnte nicht geladen werden", e);
        }
    }

    private Word logPracticeGame() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new IllegalStateException("Keine Verbindung zur DB");

            DSLContext dsl = DSL.using(conn);

            Word randomWord = getRandomWordFromDB();

            dsl.insertInto(PRACTICE_WORDLE_HISTORY)
                    .columns(PRACTICE_WORDLE_HISTORY.WORD_ID, PRACTICE_WORDLE_HISTORY.PLAYED_AT)
                    .values(randomWord.id(), LocalDateTime.now().toString())
                    .execute();

            return randomWord;
        } catch (SQLException e) {
            throw new DataAccessException("PracticeWord konnte nicht geloggt werden", e);
        }
    }

    @Override
    public String getRandomWord() {
        return logPracticeGame().text().toUpperCase();
    }

}
