package de.htw.saar.wordle.game;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;

public class Wordle {

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


    public Wordle(WordProvider provider, GameConfig config) {
        this.config = config;
        this.provider = provider;
        this.wordleWord = provider.getRandomWord();
        this.board = new Grid[config.getMaxAttempts()][config.getWordlength()];
        this.TOOL = createTool(config.getLanguage());
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

    //TODO Möglicherweise die einzelnen For-Schleifen als einzelne Methoden machen für bessere Lesbarkeit
    public void checkWord(String userInput) {
        userInput = userInput.toUpperCase();
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

}
