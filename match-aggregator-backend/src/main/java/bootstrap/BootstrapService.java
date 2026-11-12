package ru.mirea.matchaggregator.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mirea.matchaggregator.config.AppProperties;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.domain.enums.UserRole;
import ru.mirea.matchaggregator.repository.UserAccountRepository;

@Component
public class BootstrapService implements ApplicationRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public BootstrapService(UserAccountRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            AppProperties props) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailIgnoreCase(props.bootstrap().email())) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setEmail(props.bootstrap().email());
        admin.setFullName(props.bootstrap().name());
        admin.setPasswordHash(passwordEncoder.encode(props.bootstrap().password()));
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setMustChangePassword(true);
        userRepository.save(admin);
    }
}
