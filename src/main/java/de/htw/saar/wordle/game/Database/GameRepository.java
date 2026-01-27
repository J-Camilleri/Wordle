package de.htw.saar.wordle.game.Database;

import de.htw.saar.wordle.game.*;
import de.htw.saar.wordle.jooq.tables.records.GamesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.htw.saar.wordle.jooq.tables.Games.GAMES;
import static de.htw.saar.wordle.jooq.tables.Words.WORDS;

public class GameRepository {

    private static final int STATUS_LOST = 0;
    private static final int STATUS_WON = 1;
    private static final int STATUS_ACTIVE = 2;

    public boolean saveGame(int userId, Wordle game) {
        try (Connection conn = DatabaseManager.connect()){
            if(conn == null)return false;

            DSLContext dsl = DSL.using(conn);

            Integer wordId = dsl
                    .select(WORDS.ID)
                    .from(WORDS)
                    .where(WORDS.WORD_TEXT.eq(game.getWordleWord()))
                    .fetchOneInto(Integer.class);

            if (wordId == null) return false;

            Integer activeGameId = dsl
                    .select(GAMES.ID)
                    .from(GAMES)
                    .where(GAMES.USER_ID.eq(userId))
                    .and(GAMES.IS_WON.eq(STATUS_ACTIVE))
                    .fetchOneInto(Integer.class);

            GamesRecord record = (activeGameId == null)
                    ? dsl.newRecord(GAMES)
                    : dsl.fetchOne(GAMES, GAMES.ID.eq(activeGameId));

            if (record == null) return false;

            record.setUserId(userId);
            record.setWordId(wordId);
            record.setAttemptsCount(game.getAttempt());
            record.setGuesses(String.join(",", game.getGuessedWords()));
            record.setDifficulty(game.getConfig().getDifficulty().name());
            record.setIsWon(STATUS_ACTIVE);
            record.setDate(LocalDate.now().toString());

            record.store();
            return true;
        } catch (Exception e) {
        System.out.println("Fehler beim Speichern des Spiels: " + e.getMessage());
        return false;
        }
    }


    public Optional<DailyWordle> loadGame(int userId, User user) {

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return Optional.empty();

            DSLContext dsl = DSL.using(conn);

            return dsl
                    .select(
                            GAMES.ID,
                            GAMES.GUESSES,
                            GAMES.DIFFICULTY,
                            WORDS.WORD_TEXT
                    )
                    .from(GAMES)
                    .join(WORDS).on(GAMES.WORD_ID.eq(WORDS.ID))
                    .where(GAMES.USER_ID.eq(userId))
                    .and(GAMES.DATE.eq(LocalDate.now().toString()))
                    .and(GAMES.IS_WON.eq(STATUS_ACTIVE))
                    .fetchOptional(record -> {

                        int gameId = record.get(GAMES.ID);
                        String targetWord = record.get(WORDS.WORD_TEXT);
                        String guessesStr = record.get(GAMES.GUESSES);

                        Difficulty diff =
                                Difficulty.valueOf(record.get(GAMES.DIFFICULTY));

                        GameConfig config =
                                GameConfig.createThroughDifficulty(diff);

                        List<String> guesses = new ArrayList<>();
                        if (guessesStr != null && !guessesStr.isEmpty()) {
                            guesses.addAll(Arrays.asList(guessesStr.split(",")));
                        }

                        return new DailyWordle(new DailyWordleRepository(), config, user, this, gameId, targetWord, guesses);
                    });
        }catch (Exception e){
            System.out.println("Fehler beim Laden des Spiels: " + e.getMessage());
            return Optional.empty();
        }
    }

    private int getUserGameStatus(int userId) {
        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return -1;

            DSLContext dsl = DSL.using(conn);

            Integer status = dsl
                    .select(GAMES.IS_WON)
                    .from(GAMES)
                    .where(GAMES.USER_ID.eq(userId))
                    .and(GAMES.DATE.eq(LocalDate.now().toString()))
                    .limit(1)
                    .fetchOne(GAMES.IS_WON);

            return status != null ? status : -1;

        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Spielstatus: " + e.getMessage());
            return -1;
        }
    }

    public boolean hasUserActiveGame(int userId) {
        return getUserGameStatus(userId) == STATUS_ACTIVE;
    }

    public boolean isUserGameFinished(int userId) {
        int status = getUserGameStatus(userId);
        return status == STATUS_LOST || status == STATUS_WON;
    }

    public void finishGame(int userId, boolean won) {

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) return;

            DSLContext dsl = DSL.using(conn);

            dsl.update(GAMES)
                    .set(GAMES.IS_WON, won ? STATUS_WON : STATUS_LOST)
                    .where(GAMES.USER_ID.eq(userId))
                    .and(GAMES.IS_WON.eq(STATUS_ACTIVE))
                    .execute();
        } catch (Exception e) {
            System.out.println("Fehler beim Beenden des Spiels: " + e.getMessage());
        }
    }

    public static void createGamesTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS games (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            word_id INTEGER,
            attempts_count INTEGER,
            is_won INTEGER,
            guesses TEXT,
            difficulty TEXT,
            date TEXT,
            FOREIGN KEY(user_id) REFERENCES users(id),
            FOREIGN KEY(word_id) REFERENCES words(id)
        );
    """;

        try (Connection conn = DatabaseManager.connect()) {
            if (conn == null) throw new RuntimeException("Keine Verbindung zur DB");
            DSLContext dsl = DSL.using(conn);
            dsl.execute(sql);
        } catch (Exception e) {
            System.out.println("Fehler beim Erstellen der Tabelle games: " + e.getMessage());
        }
    }
}