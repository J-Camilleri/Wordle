package de.htw.saar.wordle.game;

public class AuthenticationService {

    private final UserRepository repo;

    public AuthenticationService(UserRepository repo) {
        this.repo = repo;
    }

    public void register(String username, String password) {
        String hash = PasswordHashing.hash(password);
        repo.save(username, hash);
    }

    public boolean login(String username, String password) {
        return repo.findByUsername(username)
            .map(user -> PasswordHashing.verify(password, user.passwordHash()))
            .orElse(false);
    }

    public boolean deleteAccount(String username, String password) {
        return repo.findByUsername(username)
                .filter(user -> PasswordHashing.verify(password, user.passwordHash()))
                .map(user -> repo.deleteByUsername(user.username()))
                .orElse(false);
    }
}
