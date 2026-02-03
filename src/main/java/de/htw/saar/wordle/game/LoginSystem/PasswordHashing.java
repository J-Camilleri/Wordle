package de.htw.saar.wordle.game.LoginSystem;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashing {

    //Diese Variable ist dafür da, dass man die Rechnung des Hash Passworts verlängert
    private static final int COST = 12;

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
