package de.htw.saar.wordle.game.Presentation;

import de.htw.saar.wordle.game.*;
import de.htw.saar.wordle.game.Database.GameRepository;
import de.htw.saar.wordle.game.Database.UserRepository;
import de.htw.saar.wordle.game.LoginSystem.AuthenticationService;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Dialog extends UserInterface implements GameUI {

    private Scanner input;
    private static final int LOGIN = 1;
    private static final int REGISTER = 2;
    private static final int DELETE_ACCOUNT = 3;

    private static final int DAILY_WORDLE = 1;
    private static final int PRACTICE_MODE = 2;

    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int END_GAME = 4;

    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;

    private State state = State.LOGIN_MENU;
    private boolean running = true;
    private Wordle currentGame;
    private User loggedInUser;

    UserRepository userRepository = new UserRepository();
    AuthenticationService auth = new AuthenticationService(userRepository);
    GameRepository gameRepo = new GameRepository();

    public Dialog() {
        input = new Scanner(System.in);
        currentGame = null;
    }

    public void start() {
        while (running) {
            switch (state) {
                case LOGIN_MENU -> showLoginMenu();
                case LOGIN -> handleLogin();
                case REGISTER -> handleRegister();
                case DELETE_ACCOUNT -> handleDeleteAccount();
                case MAIN_MENU -> showMainMenu();
                case DAILY_WORDLE -> handleDailyWordle();
                case DIFFICULTY -> showDifficultyMenu();
                case EXIT -> running = false;
            }
        }
    }

    private int readPositiveIntegerInput() {
        int result = -1;
        String line;

        while (result < 0) {
            try {
                line = input.nextLine();
                result = Integer.parseInt(line);
                if (result < 0)
                    System.out.println("You entered a negative integer. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error, Please enter an integer value");
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }

        }
        return result;
    }

    private void showLoginMenu() {
        System.out.println("Willkommen zu Wordle");
        System.out.println("1. Einloggen");
        System.out.println("2. Registrieren");
        System.out.println("3. Konto löschen");
        System.out.println("4. Beenden");
        System.out.println("Bitte wähle eine Option aus:");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case LOGIN -> state = State.LOGIN;
            case REGISTER -> state = State.REGISTER;
            case DELETE_ACCOUNT -> state = State.DELETE_ACCOUNT;
            case END_GAME -> state = State.EXIT;
        }
    }

    private void showMainMenu() {
        System.out.println("Bitte wähle eine Option aus");
        System.out.println("1. Daily Wordle");
        System.out.println("2. Übungsmodus");
        System.out.println("3. Beenden");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case DAILY_WORDLE -> state = State.DAILY_WORDLE;
            case PRACTICE_MODE -> state = State.DIFFICULTY;
            case END_GAME -> state = State.EXIT;
        }
    }

    //TODO Difficulty muss noch  geändert werden auf passende methoden.
    private void showDifficultyMenu() {
        System.out.println("Bitte wähle eine Schwierigkeit aus");
        System.out.println("1. Leicht");
        System.out.println("2. Mittel");
        System.out.println("3. Schwer");
        System.out.println("4. Beenden");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case EASY -> state = State.EXIT;
            case MEDIUM -> state = State.EXIT;
            case HARD -> state = State.EXIT;
            case END_GAME -> state = State.EXIT;
        }
    }

    private void handleLogin() {
        System.out.println("Benutzername: ");
        String username = input.nextLine();

        System.out.println("Passwort: ");
        String password = input.nextLine();

        auth.login(username, password).ifPresentOrElse(user -> {
            loggedInUser = user;
            System.out.println("Login Erfolgreich");
            state = State.MAIN_MENU;
        }, () -> {
            System.out.println("Benutzername oder Passwort falsch");
        });
    }

    private void handleRegister() {
        System.out.println("Benutzername:");
        String username = input.nextLine();

        System.out.println("Passwort:");
        String password = input.nextLine();

        boolean success = auth.register(username, password);
        if (success) {
            System.out.println("Registrierung erfolgreich!");
            System.out.println("Bitte Melde dich erneut an um fortzufahren");
            state = State.LOGIN;
        } else {
            System.out.println("Registrierung fehlgeschlagen: Username existiert bereits oder DB-Fehler.");
        }
    }

    private void handleDeleteAccount() {
        System.out.println("Benutzername: ");
        String username = input.nextLine();

        System.out.println("Passwort: ");
        String password = input.nextLine();

        boolean isDeleted = auth.deleteAccount(username, password);
        System.out.println(isDeleted ? "Konto erfolgreich gelöscht!" : "Benutzername oder Passwort falsch, Konto konnte nicht gelöscht werden.");
    }

    private void handleDailyWordle() {
        WordSeeder.fillIfEmpty();
        DailyWordleRepository.createDailyTable();
        DailyWordle dw = new DailyWordle(new DailyWordleRepository(), GameConfig.createThroughDifficulty(Difficulty.NORMAL),loggedInUser,gameRepo);
        dw.gameLoop();
    }

    @Override
    public void gameWon(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        } catch (NoSuchElementException e) {
            System.out.println(e);
        }
    }

    @Override
    public void gameLost(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        } catch (NoSuchElementException e) {
            System.out.println(e);
        }
    }

    @Override
    public String readWord() {
        System.out.println("Bitte gebe dein Wort ein:");
        return input.nextLine().toUpperCase();
    }

}
