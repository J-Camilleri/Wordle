package de.htw.saar.wordle.game.Presentation;

import de.htw.saar.wordle.game.AuthenticationService;
import de.htw.saar.wordle.game.UserRepository;
import de.htw.saar.wordle.game.Wordle;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Dialog extends UserInterface {

    private Scanner input;
    private static final int LOGIN = 1;
    private static final int REGISTER = 2;

    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int END_GAME = 3;

    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;

    private Wordle currentGame;

    UserRepository userRepository = new UserRepository();
    AuthenticationService auth = new AuthenticationService(userRepository);

    public Dialog() {
        input = new Scanner(System.in);
        currentGame = null;
    }

    public void start() {
        int function = 0;

        do {
            try {
                showLoginMenu();
                function = readPositiveIntegerInput();
                executeFunction(function);
            } catch(IllegalArgumentException e) {
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
            }
        }
        while(function != END_GAME);
    }

    private int readPositiveIntegerInput() {
        int result = -1;
        String line;

        while(result < 0) {
            try {
                line = input.next();
                result = Integer.parseInt(line);
                if(result < 0)
                    System.out.println("You entered a negative integer. Please try again.");
            }
            catch(NumberFormatException e) {
                System.out.println("Error, Please enter an integer value");
            }
            catch(NoSuchElementException e) {
                System.out.println(e.getMessage());
            }

        }
        return result;
    }

    private void executeFunction(int function) {
        switch(function) {
            case LOGIN:
                handleLogin();
                break;
            case REGISTER:
                handleRegister();
                break;
            case END_GAME:
                System.out.println("Du hast das Spiel geschlossen.");
                break;
            default:
                System.out.println("Ungültige Option.");
                break;
        }
    }

    private void showLoginMenu() {
        System.out.println("Willkommen zu Wordle");
        System.out.println("1. Einloggen");
        System.out.println("2. Registrieren");
        System.out.println("3. Beenden");
        System.out.println("Bitte wähle eine Option aus:");
    }

    private void showMainMenu() {
        System.out.println("Bitte wähle eine Option aus");
        System.out.println("1. Daily Wordle");
        System.out.println("2. Übungsmodus");
        System.out.println("3. Beenden");
    }

    private void showDifficultyMenu() {
        System.out.println("Bitte wähle eine Schwierigkeit aus");
        System.out.println("1. Leicht");
        System.out.println("2. Mittel");
        System.out.println("3. Schwer");
    }

    private void handleLogin() {
        System.out.println("Benutzername: ");
        String username = input.nextLine();

        System.out.println("Passwort: ");
        String password = input.nextLine();

        boolean success = auth.login(username, password);

        System.out.println(success ? "Login Erfolgreich!" : "Benutzername oder Passwort falsch.");
    }

    private void handleRegister() {
        String username = input.nextLine();
        System.out.println("Benutzername:");

        System.out.println("Passwort:");
        String password = input.nextLine();

        auth.register(username, password);

        System.out.println("Registrierung erfolgreich!");
    }

    public void gameWon(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        }
        catch (NoSuchElementException e) {
            System.out.println(e);
        }
    }

    public void gameLost(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        }
        catch (NoSuchElementException e) {
            System.out.println(e);
        }
    }
}
