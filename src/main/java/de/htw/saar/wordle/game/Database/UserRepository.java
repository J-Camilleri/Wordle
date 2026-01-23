package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.DatabaseManager;
import de.htw.saar.wordle.game.User;
import org.jooq.DSLContext;

import java.sql.*;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Scoreboard.SCOREBOARD;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;

public class UserRepository {

    public static void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            );
        """;

        try (Connection conn = DatabaseManager.connect()) {
            if (conn != null) {
                conn.createStatement().execute(sql);
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Erstellen der User-Tabelle: " + e.getMessage());
        }
    }

    public boolean save(String username, String passwordHash) {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return false;

            conn.setAutoCommit(false);
            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            try {

                int userId = dsl.insertInto(USERS)
                        .set(USERS.USERNAME, username)
                        .set(USERS.PASSWORD_HASH, passwordHash)
                        .returning(USERS.ID)
                        .fetchOne()
                        .getId();

                dsl.insertInto(SCOREBOARD)
                        .set(SCOREBOARD.USER_ID, userId)
                        .set(SCOREBOARD.SCORE, 0)
                        .execute();

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Fehler beim User-Anlegen: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.out.println("DB Fehler: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findByUsername(String username) {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return Optional.empty();

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            return dsl.selectFrom(USERS)
                    .where(USERS.USERNAME.eq(username))
                    .fetchOptional(record -> new User(
                            record.getId(),
                            record.getUsername(),
                            record.getPasswordHash()
                    ));

        } catch (Exception e) {
            System.out.println("Fehler beim Finden des Users: " + e.getMessage());
            return Optional.empty();
        }
    }



    public boolean deleteByUsername(String username) {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return false;

            DSLContext dsl = org.jooq.impl.DSL.using(conn);

            int deleted = dsl.deleteFrom(USERS)
                    .where(USERS.USERNAME.eq(username))
                    .execute();

            return deleted > 0;

        } catch (Exception e) {
            System.out.println("Fehler beim Löschen des Users: " + e.getMessage());
            return false;
        }
    }
}
