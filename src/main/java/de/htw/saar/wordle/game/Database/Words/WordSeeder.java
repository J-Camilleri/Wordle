package de.htw.saar.wordle.game.Database.Words;

import de.htw.saar.wordle.game.Database.DatabaseManager;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets; //damit auf allen System kompatibel (Windows, Linux,..)
import java.util.ArrayList;
import java.util.List;

import static de.htw.saar.wordle.jooq.tables.PracticeWords.PRACTICE_WORDS;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;


public class WordSeeder {


    public static void createWordTables() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new RuntimeException("Keine Verbindung zur DB");

            DSLContext dsl = DSL.using(conn);

            dsl.execute("""
                CREATE TABLE IF NOT EXISTS words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    word_text TEXT NOT NULL UNIQUE,
                    language_code TEXT DEFAULT 'de',
                    is_active INTEGER DEFAULT 1
                )
            """);

            dsl.execute("""
                CREATE TABLE IF NOT EXISTS practice_words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    word_text TEXT NOT NULL UNIQUE,
                    language_code TEXT DEFAULT 'de',
                    is_active INTEGER DEFAULT 1
                )
            """);

        } catch (SQLException e) {
            System.out.println("Fehler beim Erstellen der Tabellen: " + e.getMessage());
        }
    }

    public static void fillIfEmpty() {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new RuntimeException("Keine Verbindung zur DB");

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            if (isEmpty(dsl, WORDS)) {
                importFile(dsl, WORDS, "words.txt");
            }


            if (isEmpty(dsl, PRACTICE_WORDS)) {
                importFile(dsl, PRACTICE_WORDS, "words.txt");
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Seed: " + e.getMessage());
        }
    }

    public static boolean isEmpty(DSLContext dsl, org.jooq.Table<?> table) {
        int count = dsl.fetchCount(table);
        return count == 0;
    }

    /** Importiert Wörter aus einer Datei in die angegebene Tabelle */
    public static void importFile(DSLContext dsl, org.jooq.Table<?> table, String dateiPath) {
        InputStream in = WordSeeder.class.getClassLoader().getResourceAsStream(dateiPath);
        if (in == null) {
            throw new RuntimeException("Datei nicht gefunden: " + dateiPath);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> words = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim().toUpperCase());
            }


            dsl.transaction(cfg -> {
                DSLContext tx = org.jooq.impl.DSL.using(cfg);
                for (String w : words) {
                    tx.insertInto(table)
                            .columns(table.field("word_text", String.class))
                            .values(w)
                            .onDuplicateKeyIgnore()
                            .execute();
                }
            });

            System.out.println("Importiert in " + table.getName() + ": " + words.size() + " Wörter");
        } catch (IOException e) {
            System.out.println("Fehler beim importieren der Wörter in die Datenbank" + e.getMessage());
        }
    }
}

