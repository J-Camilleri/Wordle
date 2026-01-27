package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.DatabaseManager;
import de.htw.saar.wordle.game.ScoreEntry;
import org.jooq.DSLContext;

import java.util.*;
import java.sql.*;

import static de.htw.saar.wordle.jooq.tables.Scoreboard.SCOREBOARD;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;

public class ScoreboardRepository {

    public static void createScoreboard() {
        String createScoreboardSQL = """
                CREATE TABLE IF NOT EXISTS scoreboard (
                   user_id INTEGER PRIMARY KEY,
                   score INTEGER NOT NULL DEFAULT 0,
                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
                """;

        try(Connection conn = DatabaseManager.connect()) {
            if (conn == null) {
                System.out.println("Fehler: Verbindung zur Datenbank konnte nicht hergestellt werden.");
                return;
            }

            DSLContext dsl = org.jooq.impl.DSL.using(conn);
            dsl.execute(createScoreboardSQL);

        } catch (Exception e) {
            System.out.println("Fehler beim Erstellen des Scoreboards: " + e.getMessage());
        }
    }

    public static List<ScoreEntry> printScoreboard() {
        List<ScoreEntry> result = new ArrayList<>();

        try {
            Connection conn = DatabaseManager.connect();
            if (conn == null) {
                System.out.println("Fehler: Verbindung zur Datenbank konnte nicht hergestellt werden.");
                return result;
            }

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            dsl.select(USERS.USERNAME, SCOREBOARD.SCORE)
                    .from(USERS)
                    .join(SCOREBOARD).on(USERS.ID.eq(SCOREBOARD.USER_ID))
                    .orderBy(SCOREBOARD.SCORE.desc())
                    .fetch(record -> {
                        String username = record.get(USERS.USERNAME);
                        int score = record.get(SCOREBOARD.SCORE);
                        result.add(new ScoreEntry(username, score));
                        return null; // fetch braucht Rückgabewert, wir nutzen result-Liste
                    });

            System.out.println("=== SCOREBOARD ===");
            for (ScoreEntry entry : result) {
                System.out.println(entry.username() + " : " + entry.score());
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Scoreboards: " + e.getMessage());
        }

        return result;
    }



}
