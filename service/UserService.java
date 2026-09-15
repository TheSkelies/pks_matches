package pksmatches.service;

import pksmatches.model.User;
import pksmatches.model.enums.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    public User register(String username, String password, UserRole role) {
        if (findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        User user = new User(username, hash(password));
        user.setRole(role == null ? UserRole.USER : role);
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    public User authenticate(String username, String password) {
        return findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(hash(password)))
                .orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> getAll() {
        return new ArrayList<>(users);
    }

    public boolean remove(String username) {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    private String hash(String password) {
        return Integer.toHexString(password.hashCode());
    }
}
