package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Database.ScoreboardRepository;
import de.htw.saar.wordle.game.Database.UserRepository;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static de.htw.saar.wordle.jooq.tables.Users.USERS;
import static java.sql.DriverManager.getConnection;

public class DatabaseManager {

    private static String url = "jdbc:sqlite:wordle.db";


    // für Tests kann man DB switchen wie Nintendo
    public static void setDbName(String dbName) {
        url = "jdbc:sqlite:" + dbName;
    }

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            System.out.println("Fehler bei DB-Verbindung: " + e.getMessage());
            return null;
        }
    }

    public static void dbInit() {
        try (Connection conn = connect()) {
            if (conn == null) return;

            WordSeeder.createWordTables();
            ScoreboardRepository.createScoreboard();
            UserRepository.createTable();
            DailyWordleRepository.createDailyTable();
            GameRepository.createGamesTable();

        } catch (Exception e) {
            System.out.println("Fehler beim Initialisieren der DB: " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password) {
        try (Connection conn = connect()) {
            if (conn == null) return false;

            DSLContext ctx = DSL.using(conn);

            int inserted = ctx.insertInto(USERS)
                    .set(USERS.USERNAME, username)
                    .set(USERS.PASSWORD_HASH, password)
                    .execute();

            return inserted > 0;

        } catch (Exception e) {
            System.out.println("Fehler beim Anlegen des Users: " + e.getMessage());
            return false;
        }
    }
}