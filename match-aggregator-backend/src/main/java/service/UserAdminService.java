package ru.mirea.matchaggregator.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.dto.admin.UserCreateRequest;
import ru.mirea.matchaggregator.dto.admin.UserUpdateRequest;
import ru.mirea.matchaggregator.error.ConflictException;
import ru.mirea.matchaggregator.error.NotFoundException;
import ru.mirea.matchaggregator.repository.UserAccountRepository;

import java.util.UUID;

@Service
public class UserAdminService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount create(UserCreateRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        UserAccount u = new UserAccount();
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(req.role());
        u.setActive(true);
        u.setMustChangePassword(true);
        return userRepository.save(u);
    }

    @Transactional
    public UserAccount update(UUID id, UserUpdateRequest req) {
        UserAccount u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setRole(req.role());
        u.setActive(req.active());
        return userRepository.save(u);
    }

    @Transactional
    public String resetPassword(UUID id) {
        UserAccount u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        String tempPassword = "Temp" + UUID.randomUUID().toString().substring(0, 8) + "!";
        u.setPasswordHash(passwordEncoder.encode(tempPassword));
        u.setMustChangePassword(true);
        userRepository.save(u);
        return tempPassword;
    }
}
