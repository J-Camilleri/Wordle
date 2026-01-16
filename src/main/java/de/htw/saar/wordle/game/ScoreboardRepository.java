package de.htw.saar.wordle.game;

import java.sql.DriverManager;
import java.sql.*

public class ScoreboardRepository {

    public static void createScoreboard() throws SQLException {
        String createScoreboard = """
                CREATE TABLE IF NOT EXISTS scoreboard (
                   user_id INTEGER PRIMARY KEY,
                   score INTEGER NOT NULL DEFAULT 0,
                   FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """;

        try (Connection con = DatabaseManager.connect();
             Statement st = con.createStatement()){
            st.execute(createScoreboard);
        }
    }

    public static void



}
