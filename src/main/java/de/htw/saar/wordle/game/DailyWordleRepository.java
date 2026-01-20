package de.htw.saar.wordle.game;
import java.time.LocalDate;
import java.sql.*;



public class DailyWordleRepository implements WordProvider {


    public static void createDailyTable() {
        String dailyTable = """
                CREATE TABLE IF NOT EXISTS daily_words (
                    word_date TEXT PRIMARY KEY,
                    word_id INTEGER NOT NULL,
                    FOREIGN KEY (word_id) REFERENCES words(id)
                )
                """;

        try (Connection con = DatabaseManager.connect();
             Statement st = con.createStatement()) {
            st.execute(dailyTable);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


//    private static String today() {
//        return LocalDate.now().toString();
//    }

    public static Word checkExistingWords(String date) throws SQLException {
        String existingDailyWord = """
                SELECT w.id,w.word_text
                FROM daily_words dw
                JOIN words w ON w.id = dw.word_id
                WHERE dw.word_date = ?
        """;
        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(existingDailyWord)) {

            ps.setString(1, date);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String wordText = rs.getString("word_text");
                    return new Word(id, wordText);
                }
                return null;
            }
        }
    }

    private Word getRandomWordFromDB() throws SQLException {
        String sql = """
                SELECT id, word_text
                FROM words
                WHERE is_active = 1
                ORDER BY id
                LIMIT 1
                OFFSET ((strftime('%s','now') / 86400) % (SELECT COUNT(*) FROM Words))
                """;

        try (Connection con = DatabaseManager.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                int id = rs.getInt("id");
                String wordText = rs.getString("word_text");
                return new Word(id, wordText);
            }
            throw new IllegalStateException("Keine aktiven Wörter vorhanden.");
        }
    }

    public  Word chooseRandomDailyWord() throws SQLException {
        DailyWordleRepository random = new DailyWordleRepository();
        String today = LocalDate.now().toString();

        Word existing = checkExistingWords(today);

        if (existing != null) {
            return existing;
        }

        Word randomWord = random.getRandomWordFromDB();
        String insert = """
                INSERT INTO daily_words (word_date, word_id) VALUES (?, ?)""";


        try (Connection con = DatabaseManager.connect();
             PreparedStatement ps = con.prepareStatement(insert)) {
            ps.setString(1, today);
            ps.setInt(2, randomWord.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
        return randomWord;
    }

    @Override
    public String getRandomWord() {
        try {
            return chooseRandomDailyWord().text().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("DailyWord konnte nicht geladen/gespeichert werden", e);
        }
    }
}
