package ru.mirea.matchaggregator.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.config.AppProperties;
import ru.mirea.matchaggregator.domain.RefreshSession;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.dto.auth.LoginRequest;
import ru.mirea.matchaggregator.dto.auth.TokenResponse;
import ru.mirea.matchaggregator.error.NotFoundException;
import ru.mirea.matchaggregator.repository.RefreshSessionRepository;
import ru.mirea.matchaggregator.repository.UserAccountRepository;
import ru.mirea.matchaggregator.security.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final RefreshSessionRepository refreshRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties props;

    public AuthService(UserAccountRepository userRepository,
                       RefreshSessionRepository refreshRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AppProperties props) {
        this.userRepository = userRepository;
        this.refreshRepository = refreshRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.props = props;
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        UserAccount user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Учётная запись отключена");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new BadCredentialsException("Учётная запись временно заблокирована");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                user.setFailedLoginAttempts(0);
            }
            userRepository.save(user);
            throw new BadCredentialsException("Неверный логин или пароль");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String hash = hash(refreshToken);
        RefreshSession session = refreshRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Недействительный refresh-токен"));

        if (session.isRevoked() || session.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh-токен истёк или отозван");
        }

        UserAccount user = session.getUser();
        if (!user.isActive()) {
            throw new BadCredentialsException("Учётная запись отключена");
        }

        session.setRevoked(true);
        refreshRepository.save(session);

        return buildTokenResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = hash(refreshToken);
        refreshRepository.findByTokenHash(hash).ifPresent(s -> {
            s.setRevoked(true);
            refreshRepository.save(s);
        });
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Старый пароль неверен");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Новый пароль должен быть не менее 8 символов");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private TokenResponse buildTokenResponse(UserAccount user) {
        String access = jwtService.generateAccessToken(user);
        String refreshValue = jwtService.generateRefreshTokenValue();

        RefreshSession session = new RefreshSession();
        session.setUser(user);
        session.setTokenHash(hash(refreshValue));
        session.setExpiresAt(Instant.now().plus(props.jwt().refreshDays(), ChronoUnit.DAYS));
        refreshRepository.save(session);

        return new TokenResponse(
                access,
                refreshValue,
                "Bearer",
                props.jwt().accessMinutes() * 60L,
                props.jwt().refreshDays() * 24L * 60L * 60L,
                user.getEmail(),
                user.getRole().name(),
                user.isMustChangePassword()
        );
    }

    private String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка хэширования", e);
        }
    }
}
