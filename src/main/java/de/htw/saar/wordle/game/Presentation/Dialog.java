package de.htw.saar.wordle.game.Presentation;

import de.htw.saar.wordle.game.Database.*;
import de.htw.saar.wordle.game.Database.Words.WordSeeder;
import de.htw.saar.wordle.game.Logic.*;
import de.htw.saar.wordle.game.LoginSystem.AuthenticationService;
import de.htw.saar.wordle.game.LoginSystem.User;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Dialog extends UserInterface implements GameUI {

    private Scanner input;
    private static final int LOGIN = 1;
    private static final int REGISTER = 2;
    private static final int DELETE_ACCOUNT = 3;

    private static final int END_GAME = 0;
    private static final int DAILY_WORDLE = 1;
    private static final int PRACTICE_MODE = 2;
    private static final int SHOW_SCOREBOARD = 3;
    private static final int GO_BACK = 4;

    private static final int EASY = 1;
    private static final int MEDIUM = 2;
    private static final int HARD = 3;
    private Difficulty selectedDifficulty;
    final State practiceMode = State.PRACTICE_MODE;

    private State state = State.LOGIN_MENU;
    private boolean running = true;
    private User loggedInUser;
    UserRepository userRepository = new UserRepository();
    GameRepository gameRepo = new GameRepository();
    AuthenticationService auth = new AuthenticationService(userRepository);


    public Dialog() {
        input = new Scanner(System.in);
    }

    public void start() {
        DatabaseManager.dbInit();
        while (running) {
            switch (state) {
                case LOGIN_MENU -> showLoginMenu();
                case LOGIN -> handleLogin();
                case REGISTER -> handleRegister();
                case DELETE_ACCOUNT -> handleDeleteAccount();
                case MAIN_MENU -> showMainMenu();
                case SCOREBOARD -> showScoreboard();
                case DAILY_WORDLE -> handleDailyWordle();
                case PRACTICE_MODE -> handlePracticeWordle();
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
                    System.out.println("Bitte gebe eine Zahl größer 0 ein. Versuche es erneut!");
            } catch (NumberFormatException e) {
                System.out.println("Fehler, bitte geben sie gültige Zahlen ein!");
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }

        }
        return result;
    }

    private void showLoginMenu() {
        System.out.println("Willkommen zu Wordle");
        System.out.println("0. Beenden");
        System.out.println("1. Einloggen");
        System.out.println("2. Registrieren");
        System.out.println("3. Konto löschen");
        System.out.println("Bitte wähle eine Option aus:");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case END_GAME -> state = State.EXIT;
            case LOGIN -> state = State.LOGIN;
            case REGISTER -> state = State.REGISTER;
            case DELETE_ACCOUNT -> state = State.DELETE_ACCOUNT;
        }
    }

    private void showMainMenu() {
        System.out.println("Bitte wähle eine Option aus");
        System.out.println("0. Beenden");
        System.out.println("1. Daily Wordle");
        System.out.println("2. Übungsmodus (work in progress)");
        System.out.println("3. Scoreboard anzeigen");
        System.out.println("4. Zurück");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case END_GAME -> state = State.EXIT;
            case DAILY_WORDLE -> state = State.DAILY_WORDLE;
            case PRACTICE_MODE -> state = State.DIFFICULTY;
            case SHOW_SCOREBOARD -> state = State.SCOREBOARD;
            case GO_BACK -> state = State.LOGIN_MENU;
        }
    }

    private void showDifficultyMenu() {
        System.out.println("Bitte wähle eine Schwierigkeit aus");
        System.out.println("0. Beenden");
        System.out.println("1. Leicht");
        System.out.println("2. Mittel");
        System.out.println("3. Schwer");

        int choice = readPositiveIntegerInput();

        switch (choice) {
            case END_GAME -> state = State.EXIT;
            case EASY -> {
                selectedDifficulty = Difficulty.EASY;
                state = practiceMode;
            }
            case MEDIUM -> {
                selectedDifficulty = Difficulty.NORMAL;
                state = practiceMode;
            }
            case HARD -> {
                selectedDifficulty = Difficulty.HARD;
                state = practiceMode;
            }
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
        }, () -> System.out.println("Benutzername oder Passwort falsch"));
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
        if(gameRepo.isUserGameFinished(loggedInUser.id())) {
            System.out.println("Du hast heute schon dein Daily Wordle gespielt komm morgen wieder zurück!");
            state = State.EXIT;
            return;
        }

        DailyWordle game = gameRepo
                .loadGame(loggedInUser.id(), loggedInUser)
                .orElseGet(() ->
                        new DailyWordle(
                                new DailyWordleRepository(),
                                GameConfig.createThroughDifficulty(Difficulty.NORMAL),
                                loggedInUser,
                                gameRepo
                        )
                );
        game.gameLoop();
        state = State.EXIT;
    }

    private void handlePracticeWordle() {
        WordSeeder.fillIfEmpty();
        PracticeWordle game = new PracticeWordle(
                new PracticeWordleRepository(),
                GameConfig.createThroughDifficulty(selectedDifficulty),
                loggedInUser,
                gameRepo
        );
        game.gameLoop();
        state = State.MAIN_MENU;
    }

    private void showScoreboard() {
        ScoreboardRepository.printScoreboard();
        System.out.println("Press some key to go back");
        try {
            input.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
        state = State.MAIN_MENU;
    }

    @Override
    public void gameWon(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void gameLost(String message) {
        System.out.println(message);
        System.out.println("Press some key to continue");
        try {
            input.next();
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String readWord() {
        System.out.println("Bitte gebe dein Wort ein:");
        return input.nextLine().toUpperCase();
    }
}