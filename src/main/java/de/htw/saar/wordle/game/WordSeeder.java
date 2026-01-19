package de.htw.saar.wordle.game;

import java.io.InputStreamReader;
import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets; //damit auf allen System kompatibel (Windows, Linux,..)


public class WordSeeder {

    public static void fillIfEmpty() throws Exception {
        try (Connection c = DatabaseManager.connect()) {

            if (isEmpty(c, "words")) {
                importFile(c, "words", "words.txt");
            }
            if (isEmpty(c, "practice_words")) {
                importFile(c, "practice_words", "words.txt");
            }
        }
    }

    public static boolean isEmpty(Connection c, String table) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + table;

        try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {

            rs.next();
            return rs.getInt(1) == 0; //wenn 1. Spalte leer dann true
        }
    }

    public static void importFile(Connection c, String table, String dateiPath) throws Exception {
        String sql = "INSERT OR IGNORE INTO " + table + " (word_text) VALUES (?)";

        boolean oldAutoCommit = c.getAutoCommit(); //vorheriger Zustand speichern
        c.setAutoCommit(false); //erst Speichern bei commit

        InputStream in = WordSeeder.class.
                getClassLoader().
                getResourceAsStream(dateiPath);

        if (in == null) {
            throw new RuntimeException("Datei nicht gefunden: " + dateiPath);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8)
        );
             PreparedStatement ps = c.prepareStatement(sql)
        ) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String w = line.trim().toUpperCase();
                ps.setString(1, w);
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            c.commit();
            System.out.println("Importiert in " + table + ": " + count + " Wörter");

        } catch (Exception e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(oldAutoCommit);
        }
    }
}

