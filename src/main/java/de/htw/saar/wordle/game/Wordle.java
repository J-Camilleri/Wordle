package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Presentation.Dialog;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Wordle {

    private int gameId;
    private GameRepository gameRepo;
    private Dialog ui = new Dialog();
    private final GameConfig config;
    private final String wordleWord;
    private final Grid[][] board;
    private int attempt = 0;

    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GRAY = "\u001B[90m";

    private final JLanguageTool TOOL;
    private WordProvider provider;



    // Konstruktor um neues Game zu erstellen
    public Wordle(WordProvider provider, GameConfig config) {
        this.gameId = -1; // Damit der Compiler die Schniss hält, weil noch keine DB-id vorhanden
        this.config = config;
        this.provider = provider;
        this.wordleWord = provider.getRandomWord();
        this.board = new Grid[config.getMaxAttempts()][config.getWordlength()];
        this.TOOL = createTool(config.getLanguage());
    }

    // Konstruktor um Game aus der DB zu laden
    public Wordle(GameConfig config, int gameId, String targetWord, List<String> guesses) {
        this.gameId = gameId;
        this.config = config;
        this.provider = null;
        this.wordleWord = targetWord;
        this.board = new Grid[config.getMaxAttempts()][config.getWordlength()];
        this.TOOL = createTool(config.getLanguage());
        this.attempt = 0;

        for (String guess : guesses) {
            checkWord(guess);
        }
    }

    public List<String> getGuessedWords() {
        List<String> guesses = new ArrayList<>();
        for (int i = 0; i < attempt; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < config.getWordlength(); j++) {
                if (board[i][j] != null) {
                    sb.append(board[i][j].letter());
                }
            }
            guesses.add(sb.toString());
        }
        return guesses;
    }

    private static JLanguageTool createTool(String language) {
        JLanguageTool tool = new JLanguageTool(Languages.getLanguageForShortCode(language));
        for(Rule rule : tool.getAllRules()) {
            tool.disableRule(rule.getId());
        }
        for (Rule rule : tool.getAllRules()) {
            if (rule instanceof SpellingCheckRule) {
                tool.enableRule(rule.getId());
            }
        }
        return tool;
    }

    public void checkWord(){
        String userInput = ui.readWord();
        checkWord(userInput);
    }

    //TODO Möglicherweise die einzelnen For-Schleifen als einzelne Methoden machen für bessere Lesbarkeit
    public void checkWord(String userInput) {
        int length = config.getWordlength();
        boolean[] used = new boolean[length];

        if (!wordExists(userInput) || !hasAttemptsLeft()) {
            return;
        }

            for (int i = 0; i < length; i++) {
                if (userInput.charAt(i) == wordleWord.charAt(i)) {
                    board[attempt][i] = new Grid(userInput.charAt(i), LetterStatus.CORRECT);
                    used[i] = true;
                }
            }


            for(int i = 0; i < length; i++){
                if(board[attempt][i] != null) continue;

                for (int j = 0; j < length; j++) {
                    if (!used[j] && userInput.charAt(i) == wordleWord.charAt(j)) {

                        board[attempt][i] = new Grid(userInput.charAt(i), LetterStatus.PRESENT);
                        used[j] = true;
                        break;
                    }
                }
            }

        for (int i = 0; i < length; i++) {
            if (board[attempt][i] == null) {
                board[attempt][i] = new Grid(userInput.charAt(i), LetterStatus.ABSENT);
            }
        }

            attempt++;

            printBoard();

    }

    //TODO Keine Ahnung ob diese Methode in Wordle kommt oder in Dialog (tendiere aber zu Wordle)
    public void printBoard() {
        for (int r = 0; r < attempt; r++) {
            for (int c = 0; c < board[r].length; c++) {
                Grid grid = board[r][c];

                switch (grid.status()) {
                    case CORRECT -> System.out.print(GREEN + grid.letter() + RESET + " ");
                    case PRESENT -> System.out.print(YELLOW + grid.letter() + RESET + " ");
                    case ABSENT  -> System.out.print(GRAY  + grid.letter() + RESET + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }


    public boolean wordExists(String userInput)  {
        //TODO die System out prints in Dialog einbringen (Hier nur zum Debuggen genutzt)

        try{
            if (userInput.isEmpty() || userInput.contains(" ")) {
                //System.out.println("Das Wort darf nicht leer sein oder leerzeichen enthalten.");
                return false;
            }

            if (!userInput.matches("^[a-zA-Z]+$")) {
                //System.out.println("Das Wort darf nur Buchstaben von a-z beinhalten.");
                return false;
            }

            if (userInput.length() != config.getWordlength()) {
                //System.out.println("Das Wort muss aus 5 Buchstaben bestehen");
                return false;
            }

            return TOOL.check(userInput).isEmpty();

        }catch(IOException e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean gameWon(){
        if(attempt == 0){
            return false;
        }

        int lastAttempt = attempt - 1;

        for(int i = 0; i < config.getWordlength(); i++){
            Grid grid = board[lastAttempt][i];
            if(grid == null || grid.status() != LetterStatus.CORRECT){
                return false;
            }
        }

        return true;
    }

    public boolean gameLost(){
        return !gameWon() && !hasAttemptsLeft();
    }


    public boolean hasAttemptsLeft() {
        return attempt < config.getMaxAttempts();
    }

    public GameConfig getConfig() {
        return config;
    }
    public Grid[][] getBoard() {
        return board;
    }

    public String getWordleWord() {
        return wordleWord;
    }

    public int getAttempt() {
        return attempt;
    }

    public int getGameId() {
        return gameId;
    }

}
