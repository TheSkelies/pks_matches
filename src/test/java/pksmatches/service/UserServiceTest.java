package pksmatches.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pksmatches.model.User;
import pksmatches.model.enums.UserRole;
import pksmatches.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Регистрация и аутентификация с SHA-256")
    void testRegisterAndAuthenticate() {
        User registered = userService.register("testuser", "securePass123", UserRole.USER);
        assertNotNull(registered);
        assertEquals("testuser", registered.getUsername());

        // Password hash must be a 64-character SHA-256 hexadecimal string
        assertEquals(64, registered.getPasswordHash().length());
        assertNotEquals("securePass123", registered.getPasswordHash());

        // Correct password authenticates
        User authUser = userService.authenticate("testuser", "securePass123");
        assertNotNull(authUser);
        assertEquals("testuser", authUser.getUsername());

        // Incorrect password fails
        assertNull(userService.authenticate("testuser", "wrongPass"));
    }

    @Test
    @DisplayName("Запрет регистрации дублирующегося логина")
    void testDuplicateUsernameRejected() {
        userService.register("admin_new", "pass1", UserRole.ADMIN);
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("admin_new", "pass2", UserRole.USER));
    }

    static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> store = new HashMap<>();
        private int idGen = 1;

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(store.get(username.toLowerCase()));
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public User save(User user) {
            user.setId(idGen++);
            store.put(user.getUsername().toLowerCase(), user);
            return user;
        }

        @Override
        public void update(User user) {
            store.put(user.getUsername().toLowerCase(), user);
        }

        @Override
        public void deleteByUsername(String username) {
            store.remove(username.toLowerCase());
        }
    }
}
