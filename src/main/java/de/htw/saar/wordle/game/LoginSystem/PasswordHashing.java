package de.htw.saar.wordle.game.LoginSystem;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashing {

    /**
    This Variable is there to increase the time for the hash to be calculated
    the higher the number the longer it takes for a potential attacker to get the password
     */
    private static final int COST = 12;

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
