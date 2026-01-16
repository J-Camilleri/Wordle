package de.htw.saar.wordle.game;

import java.sql.*;
import java.util.Optional;

public class UserRepository {

    public static void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            );
        """;

        try (Connection con = DatabaseManager.connect();
             Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    public boolean save(String username, String passwordHash) {

        String insertUser = "INSERT INTO users(username, password_hash) VALUES (?, ?)";
        String insertScore = "INSERT INTO scoreboard(user_id, score) VALUES (?, 0)";

        try (Connection connection = DatabaseManager.connect()) {
            connection.setAutoCommit(false); //Damit in Scoreboard direkt mit angelegt wird und zb kein User ohne Score angelegt wird (Transaktion startet)
            //wenn  AutoCommit = false muss irgendwo commit oder rollback vorkommen sonst weiß DB nicht ob Speichern oder löschen

            try {
                int userId;

                //User anlegen
                try (PreparedStatement psUser = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, username);
                    psUser.setString(2, passwordHash);
                    psUser.executeUpdate();

                    try (ResultSet rs = psUser.getGeneratedKeys()) {  //ID holen für Scoreboard
                        if (!rs.next()) {
                            throw new SQLException("Keine User_ID erzeugt");
                        }
                        userId = rs.getInt(1);
                    }

                }
                //Scoreboardeintrag anlegen
                try (PreparedStatement psScore = connection.prepareStatement(insertScore)) {
                    psScore.setInt(1, userId);
                    psScore.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.out.println("Rollback fehler" + ex.getMessage());
                }
                System.out.println("Fehler beim User-Anlegen: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            //Fehler beim connect oder close der connection
            System.out.println("DB Fehler: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                ));
            }
            return Optional.empty();
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    public boolean deleteByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            int affectedRows = ps.executeUpdate();

            return affectedRows > 0;

        } catch(SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
