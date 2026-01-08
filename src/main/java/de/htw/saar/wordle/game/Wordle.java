package de.htw.saar.wordle.game;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

public class Wordle {

    JLanguageTool ln = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));

}
