package ru.mirea.matchaggregator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.matchaggregator.domain.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
