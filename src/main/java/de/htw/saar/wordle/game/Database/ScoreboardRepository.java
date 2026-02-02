package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.Database.Score.ScoreEntry;
import de.htw.saar.wordle.game.Exceptions.DataAccessException;
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
                throw new DataAccessException("Fehler: Verbindung zur Datenbank konnte nicht hergestellt werden.");
            }

            DSLContext dsl = org.jooq.impl.DSL.using(conn);
            dsl.execute(createScoreboardSQL);

        } catch (SQLException e) {
            throw new DataAccessException("Fehler beim Erstellen des Scoreboards: ", e);
        }
    }

    public static List<ScoreEntry> printScoreboard() {
        List<ScoreEntry> result = new ArrayList<>();

        try {
            Connection conn = DatabaseManager.connect();
            if (conn == null) {
                throw new DataAccessException("Fehler: Verbindung zur Datenbank konnte nicht hergestellt werden.");
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

        } catch (DataAccessException e) {
            throw new DataAccessException("Fehler beim laden des Scoreboards ", e);
        }

        return result;
    }

    public static void updateScore(int userId, int points) {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) {
                throw new DataAccessException("Fehler: Verbindung zur Datenbank konnte nicht hergestellt werden.");
            }
            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            boolean exists = dsl.fetchExists(
                    dsl.selectFrom(SCOREBOARD)
                            .where(SCOREBOARD.USER_ID.eq(userId))
            );

            if (exists) {
                dsl.update(SCOREBOARD)
                        .set(SCOREBOARD.SCORE, SCOREBOARD.SCORE.plus(points))
                        .where(SCOREBOARD.USER_ID.eq(userId))
                        .execute();
            } else {
                dsl.insertInto(SCOREBOARD, SCOREBOARD.USER_ID, SCOREBOARD.SCORE)
                        .values(userId, points)
                        .execute();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Fehler beim Aktualisieren des Scoreboards: ", e);
        }
    }

}