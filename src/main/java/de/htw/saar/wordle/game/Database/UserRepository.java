package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.Exceptions.DataAccessException;
import de.htw.saar.wordle.game.LoginSystem.User;
import org.jooq.DSLContext;

import java.sql.*;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Scoreboard.SCOREBOARD;
import static de.htw.saar.wordle.jooq.tables.Users.USERS;

public class UserRepository {

    public static void createTable() {
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
            throw new DataAccessException("Fehler beim Erstellen der User-Tabelle: ", e);
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
                throw new DataAccessException("Fehler beim User anlegen ", e);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Verbindung zur Datenbank konnte nicht erstellt werden ", e);
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
            throw new DataAccessException("User konnte nicht gefunden werden", e);
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
            throw new DataAccessException("User konnte nicht gelöscht werden", e);
        }
    }
}
