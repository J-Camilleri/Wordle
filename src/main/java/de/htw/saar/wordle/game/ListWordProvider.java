package de.htw.saar.wordle.game;

import java.util.Random;

public class ListWordProvider implements WordProvider {

    private static final String[] WORD_LIST = {
            "Apfel", "Tiere", "Clown", "Handy", "mango",
            "feier", "beere", "pause", "braun", "farbe"
    };

    private Random rand = new Random();


    @Override
    public String getRandomWord() {
        return WORD_LIST[rand.nextInt(WORD_LIST.length)].toUpperCase();
    }

}
