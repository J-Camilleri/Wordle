package de.htw.saar.wordle.game;

import de.htw.saar.wordle.game.LoginSystem.PasswordHashing;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingTest {

    @Test
    void samePasswordDifferentHash() {
        String h1 = PasswordHashing.hash("secret");
        String h2 = PasswordHashing.hash("secret");

        assertNotEquals(h1, h2);
    }

    @Test
    void testCorrectPassword() {
        String hash = PasswordHashing.hash("mypassword");
        assertTrue(PasswordHashing.verify("mypassword", hash));
    }

    @Test
    void testIncorrectPassword() {
        String hash = PasswordHashing.hash("correct");
        assertFalse(PasswordHashing.verify("wrong", hash));
    }
}
