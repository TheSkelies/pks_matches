package pksmatches.service;

import pksmatches.model.User;
import pksmatches.model.enums.UserRole;
import pksmatches.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        User user = new User(username, hash(password));
        user.setRole(role == null ? UserRole.USER : role);
        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPasswordHash().equals(hash(password)))
                .orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public boolean remove(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return false;
        userRepository.deleteByUsername(username);
        return true;
    }

    private String hash(String password) {
        return Integer.toHexString(password.hashCode());
    }
}