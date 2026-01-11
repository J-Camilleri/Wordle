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
            .map(user ->
                PasswordHashing.verify(
                    password,
                    user.getPasswordHash()))
            .orElse(false);
    }
}
