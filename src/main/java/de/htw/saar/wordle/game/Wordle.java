package de.htw.saar.wordle.game;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;

public class Wordle {

    private String wordleWord; //TODO Keine Ahnung ob ich das hier brauch
    private char  [][] wordleGrid;
    private char [][] wordStatusGrid;
    private int attempt = 0;
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GRAY = "\u001B[90m";
    private static final JLanguageTool TOOL = createTool();
    private WordProvider provider;


    public Wordle(WordProvider provider) {
        this.provider = provider;
        this.wordleWord = provider.getRandomWord();
        wordleGrid = new char[6][wordleWord.length()];
        wordStatusGrid = new char[6][wordleWord.length()];
    }

    private static JLanguageTool createTool() {
        JLanguageTool tool = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
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

    public void checkWord(String userInput) {
        userInput = userInput.toUpperCase();
        int length = wordleWord.length();
        boolean[] used = new boolean[length];

        if(wordExists(userInput)){

            for(int i = 0; i < length; i++){
                wordleGrid[attempt][i] = userInput.charAt(i);
            }

            for(int i = 0; i < length; i++){
                if(userInput.charAt(i) == wordleWord.charAt(i)){
                 wordStatusGrid[attempt][i] = 'G';
                 used[i] = true;
                }
            }

            for (int i = 0; i < length; i++) {
                if (wordStatusGrid[attempt][i] == 'G') continue;

                for (int j = 0; j < length; j++) {
                    if (!used[j] && userInput.charAt(i) == wordleWord.charAt(j)) {
                        wordStatusGrid[attempt][i] = 'Y';
                        used[j] = true;
                        break;
                    }
                }
            }

            for (int i = 0; i < length; i++) {
                if (wordStatusGrid[attempt][i] == '\0') {
                    wordStatusGrid[attempt][i] = 'X';
                }
            }

            attempt++;

            printBoard();
        }
    }

    //TODO diese Methode ist da um immer wieder die checkWord Methode zu wiederholen
    //TODO bis das spiel gewonnen oder Verloren ist
    private void gameLoop() {

    }

    //TODO Keine Ahnung ob diese Methode in Wordle kommt oder in Dialog (tendiere aber zu Wordle)
    public void printBoard() {
        for (int r = 0; r < attempt; r++) { // zeigt jetzt nur die aktuellen Versuche an
            for (int c = 0; c < wordleGrid[r].length; c++) {

                char letter = wordleGrid[r][c];
                char s = wordStatusGrid[r][c];

                switch (s) {
                    case 'G' -> System.out.print(GREEN + letter + RESET + " ");
                    case 'Y' -> System.out.print(YELLOW + letter + RESET + " ");
                    case 'X' -> System.out.print(GRAY + letter + RESET + " ");
                    default -> System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }


    public static boolean wordExists(String userInput) {
        //TODO die System out prints in Dialog einbringen (Hier nur zum Debuggen genutzt)
        try {
            if (userInput.isEmpty() || userInput.contains(" ")) {
                //System.out.println("Das Wort darf nicht leer sein oder leerzeichen enthalten.");
                return false;
            }

            if (!userInput.matches("^[a-zA-Z]+$")) {
                //System.out.println("Das Wort darf nur Buchstaben von a-z beinhalten.");
                return false;
            }

            if (userInput.length() != 5) {
                //System.out.println("Das Wort muss aus 5 Buchstaben bestehen");
                return false;
            }
            return TOOL.check(userInput).isEmpty();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public char[][] getWordleGrid() {
        return wordleGrid;
    }

    public void setWordleGrid(char[][] wordleGrid) {
        this.wordleGrid = wordleGrid;
    }

    public char[][] getWordStatusGrid() {
        return wordStatusGrid;
    }

    public void setWordStatusGrid(char[][] wordStatusGrid) {
        this.wordStatusGrid = wordStatusGrid;
    }
}
