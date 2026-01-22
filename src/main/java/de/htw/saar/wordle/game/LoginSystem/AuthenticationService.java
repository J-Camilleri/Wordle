package de.htw.saar.wordle.game.LoginSystem;

import de.htw.saar.wordle.game.Database.UserRepository;
import de.htw.saar.wordle.game.User;

import java.util.Optional;

public class AuthenticationService {

    private final UserRepository repo;

    public AuthenticationService(UserRepository repo) {
        this.repo = repo;
    }

    public boolean register(String username, String password) {
        String hash = PasswordHashing.hash(password);
        return repo.save(username, hash);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = repo.findByUsername(username);

        if (userOpt.isPresent() &&
                PasswordHashing.verify(password, userOpt.get().passwordHash())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public boolean deleteAccount(String username, String password) {
        return repo.findByUsername(username)
                .filter(user -> PasswordHashing.verify(password, user.passwordHash()))
                .map(user -> repo.deleteByUsername(user.username()))
                .orElse(false);
    }
}
