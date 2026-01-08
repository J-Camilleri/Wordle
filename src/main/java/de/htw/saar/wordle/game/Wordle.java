package de.htw.saar.wordle.game;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;

public class Wordle {

    private String[][] wordleGrid;
    private static final JLanguageTool TOOL = createTool();

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

    public static boolean wordExists(String userInput) throws IOException {
        //TODO die System out prints in Dialog einbringen (Hier nur zum Debuggen genutzt)

        if (userInput.isEmpty() || userInput.contains(" ")) {
           //System.out.println("Das Wort darf nicht leer sein oder leerzeichen enthalten.");
            return false;
        }

        if(!userInput.matches("^[a-zA-Z]+$")) {
            //System.out.println("Das Wort darf nur Buchstaben von a-z beinhalten.");
            return false;
        }

        if(userInput.length() != 5) {
            //System.out.println("Das Wort muss aus 5 Buchstaben bestehen");
            return false;
        }

        return TOOL.check(userInput).isEmpty();
    }
}
