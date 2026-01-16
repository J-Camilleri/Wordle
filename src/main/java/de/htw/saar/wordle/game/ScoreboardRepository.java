package de.htw.saar.wordle.game;

import java.util.*;
import java.sql.DriverManager;
import java.sql.*;

public class ScoreboardRepository {

    public static void createScoreboard() throws SQLException {
        String createScoreboard = """
                CREATE TABLE IF NOT EXISTS scoreboard (
                   user_id INTEGER PRIMARY KEY,
                   score INTEGER NOT NULL DEFAULT 0,
                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE 
                );
                """;
        //CASCADE damit wenn wir User löschen auch den score löschen

        try (Connection con = DatabaseManager.connect();
             Statement st = con.createStatement()){
            st.execute(createScoreboard);
        }
    }

    public static List <ScoreEntry> printScoreboard() throws SQLException {
        String sortScores = """
                SELECT u.username, s.score
                FROM users u
                JOIN scoreboard s ON u.id = s.user_id
                ORDER BY s.score DESC
        """;
        //JOIN um Username mit passender ID zu nehmen

        List<ScoreEntry> result = new ArrayList<>();

        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sortScores);
             ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                result.add(new ScoreEntry(username, score));
            }
        }

        System.out.println("=== SCOREBOARD ===");
        for (ScoreEntry entry : result) {
            System.out.println(entry.username() + " : " + entry.score());
        }
        return result;
    }



}
